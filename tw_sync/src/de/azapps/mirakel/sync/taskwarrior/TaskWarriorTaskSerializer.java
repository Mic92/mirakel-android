/*******************************************************************************
 * Mirakel is an Android App for managing your ToDo-Lists
 * 
 * Copyright (c) 2013-2014 Anatolij Zelenin, Georg Semmler.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.azapps.mirakel.sync.taskwarrior;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import de.azapps.mirakel.DefinitionsHelper.SYNC_STATE;
import de.azapps.mirakel.helper.DateTimeHelper;
import de.azapps.mirakel.model.tags.Tag;
import de.azapps.mirakel.model.task.Task;
import de.azapps.mirakel.sync.R;

public class TaskWarriorTaskSerializer implements JsonSerializer<Task> {

	private final Context mContext;

	public TaskWarriorTaskSerializer(final Context ctx) {
		this.mContext = ctx;
	}

	private String formatCal(final Calendar c) {
		final SimpleDateFormat df = new SimpleDateFormat(
				this.mContext.getString(R.string.TWDateFormat));
		if (c.getTimeInMillis() < 0) {
			c.setTimeInMillis(10);
		}
		return df.format(c.getTime());
	}

	private static String escape(final String string) {
		return string.replace("\"", "\\\"");
	}

	private static String cleanQuotes(String str) {
		// call this only if string starts and ands with "
		// additional keys has this
		if (str.startsWith("\"") || str.startsWith("'")) {
			str = str.substring(1);
		}
		if (str.endsWith("\"") || str.endsWith("'")) {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

	@Override
	public JsonElement serialize(final Task task, final Type arg1,
			final JsonSerializationContext arg2) {
		final JsonObject json = new JsonObject();
		final Map<String, String> additionals = task.getAdditionalEntries();
		String end = null;
		String status = "pending";
		final Calendar now = new GregorianCalendar();
		now.setTimeInMillis(now.getTimeInMillis()
				- DateTimeHelper.getTimeZoneOffset(true, now));
		if (task.getSyncState() == SYNC_STATE.DELETE) {
			status = "deleted";
			end = formatCal(now);
		} else if (task.isDone()) {
			status = "completed";
			if (additionals.containsKey("end")) {
				end = cleanQuotes(additionals.get("end"));
			} else {
				end = formatCal(now);
			}
		} else if (task.getAdditionalEntries().containsKey("status")) {
			status = cleanQuotes(task.getAdditionalEntries().get("status"));
		}

		String priority = null;
		switch (task.getPriority()) {
		case -2:
		case -1:
			priority = "L";
			break;
		case 1:
			priority = "M";
			break;
		case 2:
			priority = "H";
			break;
		default:
			break;
		}

		String uuid = task.getUUID();
		if (uuid == null || uuid.trim().equals("")) {
			uuid = java.util.UUID.randomUUID().toString();
			task.setUUID(uuid);
			task.save(false);
		}
		json.addProperty("uuid", uuid);
		json.addProperty("status", status);
		json.addProperty("entry", formatCalUTC(task.getCreatedAt()));
		json.addProperty("description", escape(task.getName()));

		if (task.getDue() != null) {
			json.addProperty("due", formatCalUTC(task.getDue()));

		}
		if (task.getList() != null
				&& !additionals.containsKey(TaskWarriorSync.NO_PROJECT)) {
			json.addProperty("project", task.getList().getName());
		}
		if (priority != null) {
			json.addProperty("priority", priority);
			if ("L".equals(priority) && task.getPriority() != -2) {
				json.addProperty("priorityNumber", task.getPriority());
			}
		}
		if (task.getUpdatedAt() != null) {
			json.addProperty("modified", formatCalUTC(task.getUpdatedAt()));
		}
		if (task.getReminder() != null) {
			json.addProperty("reminder", formatCalUTC(task.getReminder()));
		}

		if (end != null) {
			json.addProperty("end", end);
		}
		if (task.getProgress() != 0) {
			json.addProperty("progress", task.getProgress());
		}
		// Tags
		if (task.getTags().size() > 0) {
			final JsonArray tags = new JsonArray();
			for (final Tag t : task.getTags()) {
				// taskwarrior does not like whitespaces
				tags.add(new JsonPrimitive(t.getName().trim().replace(" ", "_")));
			}
			json.add("tags", tags);
		}
		// End Tags
		// Annotations
		if (task.getContent() != null && !task.getContent().equals("")) {
			final JsonArray annotations = new JsonArray();
			/*
			 * An annotation in taskd is a line of content in Mirakel!
			 */
			final String annotationsList[] = escape(task.getContent()).split(
					"\n");
			final Calendar d = task.getUpdatedAt();

			for (final String a : annotationsList) {
				final JsonObject line = new JsonObject();
				line.addProperty("entry", formatCalUTC(task.getUpdatedAt()));
				line.addProperty("description", a.replace("\n", ""));
				annotations.add(line);
				d.add(Calendar.SECOND, 1);
			}
			json.add("annotations", annotations);
		}
		// Anotations end
		// TW.depends==Mirakel.subtasks!
		// Dependencies
		if (task.getSubtaskCount() > 0) {
			boolean first1 = true;
			String depends = "";
			for (final Task subtask : task.getSubtasks()) {
				if (first1) {
					first1 = false;
				} else {
					depends += ",";
				}
				depends += subtask.getUUID();
			}
			json.addProperty("depends", depends);
		}
		// end Dependencies
		// Additional Strings
		if (additionals != null) {
			for (final String key : additionals.keySet()) {
				if (!key.equals(TaskWarriorSync.NO_PROJECT)
						&& !key.equals("status")) {
					json.addProperty(key, cleanQuotes(additionals.get(key)));
				}
			}
		}
		// end Additional Strings
		return json;
	}

	private String formatCalUTC(final Calendar c) {
		return formatCal(DateTimeHelper.getUTCCalendar(c));
	}
}
