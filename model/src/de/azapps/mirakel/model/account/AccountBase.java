package de.azapps.mirakel.model.account;

import android.content.ContentValues;
import de.azapps.mirakel.model.DatabaseHelper;
import de.azapps.mirakel.model.account.AccountMirakel.ACCOUNT_TYPES;

class AccountBase {
	public final static String TYPE = "type";
	public final static String ENABLED = "enabled";
	public static final String SYNC_KEY = "sync_key";

	private int id;
	private String name;
	private int type;
	private boolean enabled;
	private String syncKey;

	public AccountBase(final int id, final String name,
			final ACCOUNT_TYPES type, final boolean enabled,
			final String syncKey) {
		this.setId(id);
		this.setName(name);
		this.setType(type.toInt());
		this.setEnabeld(enabled);
		this.setSyncKey(syncKey);
	}

	public int getId() {
		return this.id;
	}

	protected void setId(final int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public ACCOUNT_TYPES getType() {
		return ACCOUNT_TYPES.parseInt(this.type);
	}

	public void setType(final int type) {
		this.type = type;
	}

	public ContentValues getContentValues() {
		final ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.ID, this.id);
		cv.put(DatabaseHelper.NAME, this.name);
		cv.put(TYPE, this.type);
		cv.put(ENABLED, this.enabled);
		cv.put(SYNC_KEY, this.syncKey);
		return cv;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabeld(final boolean enabeld) {
		this.enabled = enabeld;
	}

	public String getSyncKey() {
		return this.syncKey;
	}

	public void setSyncKey(final String syncKey) {
		this.syncKey = syncKey;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.id;
		result = prime * result + (this.enabled ? 1231 : 1237);
		result = prime * result
				+ (this.name == null ? 0 : this.name.hashCode());
		result = prime * result
				+ (this.syncKey == null ? 0 : this.syncKey.hashCode());
		result = prime * result + this.type;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AccountBase)) {
			return false;
		}
		final AccountBase other = (AccountBase) obj;
		if (this.id != other.id) {
			return false;
		}
		if (this.enabled != other.enabled) {
			return false;
		}
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.syncKey == null) {
			if (other.syncKey != null) {
				return false;
			}
		} else if (!this.syncKey.equals(other.syncKey)) {
			return false;
		}
		if (this.type != other.type) {
			return false;
		}
		return true;
	}

}
