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
package de.azapps.mirakel.model.list.meta;

import java.util.List;

import android.content.Context;
import de.azapps.mirakel.model.DatabaseHelper;
import de.azapps.mirakel.model.R;
import de.azapps.mirakel.model.tags.Tag;

public class SpecialListsTagProperty extends SpecialListsSetProperty {

	public SpecialListsTagProperty(final boolean isNegated,
			final List<Integer> content) {
		super(isNegated, content);
	}

	@Override
	protected String propertyName() {
		return Tag.TABLE;
	}

	@Override
	public String getWhereQuery() {
		String query = this.isNegated ? " NOT " : "";
		query += DatabaseHelper.ID + " IN (";
		query += "SELECT task_id FROM " + Tag.TAG_CONNECTION_TABLE;
		query += " WHERE tag_id IN(";
		query = addContent(query);
		return query + "))";
	}

	@Override
	public String getSummary(final Context ctx) {
		String summary = this.isNegated ? ctx.getString(R.string.not_in) : "";
		boolean first = true;
		for (final int p : this.content) {
			final Tag t = Tag.getTag(p);
			if (t == null) {
				continue;
			}
			summary += (first ? "" : ",") + t;
			if (first) {
				first = false;
			}
		}
		return summary;
	}

}
