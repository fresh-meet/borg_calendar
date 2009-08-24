package net.sf.borg.model.tool;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Vector;

import net.sf.borg.common.DateUtil;
import net.sf.borg.model.LinkModel;
import net.sf.borg.model.db.jdbc.ApptJdbcDB;
import net.sf.borg.model.db.jdbc.JdbcDB;
import net.sf.borg.model.entity.Appointment;

/**
 * 
 * converts all appointments in a 1.7.1 database to use a simple, sequential
 * primary key (like other objects) for version 1.7.2. In versions 1.7.1 and
 * before, the appointment primary key was an encoded date, which forced a
 * primary key change whenever an appointment changed date.
 * 
 * 
 */
public class ApptKeyConverter {

	public static void main(String[] args) {
		try {
			// init cal model & load data from database
			String dbdir = JdbcDB.buildDbDir();

			if (dbdir.equals("not-set")) {
				return;
			}
			JdbcDB.connect(dbdir);
			new ApptKeyConverter().convert();
			JdbcDB.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * run the conversion. this method expects the JDBC connection to already be established
	 * in JdbcDB
	 * @throws Exception
	 */
	public void convert() throws Exception {
		
		// try to remove extra palm columns
		try{JdbcDB.execSQL("ALTER table appointments drop new");}catch(Exception e){e.printStackTrace();}
		try{JdbcDB.execSQL("ALTER table appointments drop deleted");}catch(Exception e){e.printStackTrace();}
		try{JdbcDB.execSQL("ALTER table addresses drop new");}catch(Exception e){}
		try{JdbcDB.execSQL("ALTER table addresses drop deleted");}catch(Exception e){}
		try{JdbcDB.execSQL("ALTER table memos drop new");}catch(Exception e){}
		try{JdbcDB.execSQL("ALTER table memos drop deleted");}catch(Exception e){}

		// go directly to jdbc. going through the model, which maintains all
		// kinds
		// of maps and caches is too slow when doing a bulk update like this.
		ApptJdbcDB db = new ApptJdbcDB();

		try {

			db.beginTransaction();

			int lowestKey = 1;

			// get all appointments
			Collection<Appointment> appts = db.readAll();
			for (Appointment oldappt : appts) {

				// skip already converted appts
				// the 1.7.1 key format always was >10000000 due to the way the
				// date was
				// encoded
				if (oldappt.getKey() < 10000000) {
					continue;
				}

				System.out.println("Processing appt: " + oldappt.getKey());

				// find the available lowest key - brute force
				// after the conversion, when the old keys are gone, SQL
				// MAX(appt_num)+1 can find the
				// next available key very easily
				while (true) {
					Appointment ap = db.readObj(lowestKey);
					if (ap == null)
						break;
				}

				System.out.println("New Key: " + lowestKey);

				Appointment newappt = oldappt.copy();
				newappt.setKey(lowestKey);

				// adjust skip list to use a new format for dates
				// instead of the old date encoding
				Vector<String> skipList = newappt.getSkipList();
				if (skipList != null && skipList.size() > 0) {
					for (int i = 0; i < skipList.size(); i++) {
						String key = skipList.elementAt(i);
						int val = Integer.parseInt(key);
						if (val < 900000)
							continue;

						// this stupid math is one reason the old format sucked
						int yr = (val / 1000000) % 1000 + 1900;
						int mo = (val / 10000) % 100 - 1;
						int day = (val / 100) % 100;

						Calendar cal = new GregorianCalendar();
						cal.set(yr, mo, day);

						// replace the skiplist date with the new format
						skipList.setElementAt(Integer.toString(DateUtil
								.dayOfEpoch(cal.getTime())), i);

					}
					newappt.setSkipList(skipList);
				}

				// add the converted appointment
				db.addObj(newappt);

				// move any links from the old appt to the new one
				LinkModel.getReference().moveLinks(oldappt, newappt);

				// delete the old appt
				db.delete(oldappt.getKey());

				lowestKey++;
			}

			db.commitTransaction();

		} catch (Exception e) {
			e.printStackTrace();
			db.rollbackTransaction();
		} finally {

		}
	}

}