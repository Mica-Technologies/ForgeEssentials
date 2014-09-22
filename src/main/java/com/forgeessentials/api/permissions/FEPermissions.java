package com.forgeessentials.api.permissions;


public final class FEPermissions {

	public static final String MSG_NO_COMMAND_PERM = "You don't have permissions to use this command!";
	public static final String MSG_NO_PERM = "You don't have permissions for that!";
	public static final String MSG_NO_CONSOLE_COMMAND = "This command cannot be invoked from console";

	// ------------------------------------------------
	// -- Internal properties
	// ------------------------------------------------

	public static final String FE_INTERNAL = "fe.internal";

	public static final String ZONE_ENTRY_MESSAGE = FE_INTERNAL + ".zone.entry";
	public static final String ZONE_EXIT_MESSAGE = FE_INTERNAL + ".zone.exit";

	public static final String PREFIX = FE_INTERNAL + ".prefix";
	public static final String SUFFIX = FE_INTERNAL + ".suffix";

	public static final String GROUP = FE_INTERNAL + ".group";
	public static final String GROUP_ID = GROUP + ".id";
	public static final String GROUP_PRIORITY = GROUP + ".priority";

	public static final int GROUP_PRIORITY_DEFAULT = 10;

	public static final String PLAYER = FE_INTERNAL + ".player";
	public static final String PLAYER_UUID = PLAYER + ".uuid";
	public static final String PLAYER_NAME = PLAYER + ".name";
	public static final String PLAYER_GROUPS = PLAYER + ".groups";

}