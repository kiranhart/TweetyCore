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

package ca.tweetzy.core.utils.colors.patterns;


import ca.tweetzy.core.utils.colors.ColorFormatter;
import ca.tweetzy.core.utils.colors.ColorPattern;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date Created: April 02 2022
 * Time Created: 11:17 a.m.
 *
 * @author Kiran Hart
 */
public final class SolidColorPattern implements ColorPattern {
	Pattern pattern = Pattern.compile("&?#([0-9A-Fa-f]{6})");

	/**
	 * Applies a solid RGB color to the provided String.
	 * Output might me the same as the input if this pattern is not present.
	 *
	 * @param string The String to which this pattern should be applied to
	 * @return The new String with applied pattern
	 */
	public String process(String string) {
		Matcher matcher = pattern.matcher(string);
		while (matcher.find()) {
			String color = matcher.group(1);
			if (color == null) color = matcher.group(2);

			string = string.replace(matcher.group(), ColorFormatter.getColor(color) + "");
		}
		return string;
	}
}
