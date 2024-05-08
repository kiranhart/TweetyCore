/*
 * Flight
 * Copyright 2022 Kiran Hart
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ca.tweetzy.core.compatibility;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;

public enum ServerVersion {
	UNKNOWN("Unknown"),
	V1_7("1.7"),
	V1_8("1.8"),
	V1_9("1.9"),
	V1_10("1.10"),
	V1_11("1.11"),
	V1_12("1.12"),
	V1_13("1.13"),
	V1_14("1.14"),
	V1_15("1.15"),
	V1_16("1.16"),
	V1_17("1.17"),
	V1_18("1.18"),
	V1_19("1.19"),
	V1_20("1.20"),
	V1_21("1.21"),
	V1_22("1.22"),
	V1_23("1.23");

	private final String versionName;

	ServerVersion(String versionName) {
		this.versionName = versionName;
	}

	public String getVersionName() {
		return versionName;
	}

	public static ServerVersion getVersion() {
		String[] versionPkgRaw = Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.");
		String versionPkg = versionPkgRaw[0] + "." + versionPkgRaw[1];

		for (ServerVersion version : values()) {
			if (versionPkg.equalsIgnoreCase(version.getVersionName())) {
				return version;
			}
		}

		return UNKNOWN;
	}

	public boolean isLessThan(ServerVersion other) {
		return this.ordinal() < other.ordinal();
	}

	public boolean isAtOrBelow(ServerVersion other) {
		return this.ordinal() <= other.ordinal();
	}

	public boolean isGreaterThan(ServerVersion other) {
		return this.ordinal() > other.ordinal();
	}

	public boolean isAtLeast(ServerVersion other) {
		return this.ordinal() >= other.ordinal();
	}

	public static boolean isServerVersion(ServerVersion version) {
		return getVersion() == version;
	}

	public static boolean isServerVersion(ServerVersion... versions) {
		return ArrayUtils.contains(versions, getVersion());
	}

	public static boolean isServerVersionAbove(ServerVersion version) {
		return getVersion().ordinal() > version.ordinal();
	}

	public static boolean isServerVersionAtLeast(ServerVersion version) {
		return getVersion().ordinal() >= version.ordinal();
	}

	public static boolean isServerVersionAtOrBelow(ServerVersion version) {
		return getVersion().ordinal() <= version.ordinal();
	}

	public static boolean isServerVersionBelow(ServerVersion version) {
		return getVersion().ordinal() < version.ordinal();
	}
}
