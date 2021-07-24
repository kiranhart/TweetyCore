package ca.tweetzy.core.hooks;

/**
 * The current file has been created by Kiran Hart
 * Date Created: July 24 2021
 * Time Created: 3:17 a.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public interface Hook {

    abstract String getName();

    abstract boolean isEnabled();
}
