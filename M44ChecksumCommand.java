package com.memoir44.vassal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GameState;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.tools.FormattedString;

public class M44ChecksumCommand extends Command {
	public static final String COMMAND_PREFIX = "GAMECHECKSUM\t";
	protected String checksumValue;
	protected Boolean silentCheck;

	public M44ChecksumCommand(String subString) {
		String[] parts = subString.split("\\t");
		this.checksumValue = parts[0];
		this.silentCheck = Boolean.valueOf(parts[1]);
	}

	public M44ChecksumCommand(String checksumValue, Boolean silentCheck) {
		this.checksumValue = checksumValue;
		this.silentCheck = silentCheck;
	}

	public String getChecksumValue() {
		return checksumValue;
	}

	private java.util.Timer commandDelay = new java.util.Timer();
	private java.util.TimerTask executeCommandDelayListener = new java.util.TimerTask() {
		@Override
		public void run() {
			final GameModule mod = GameModule.getGameModule();

			String chk = calculateChecksum();

			if (chk.equals(checksumValue)) {
				if (!silentCheck) {
					WriteLine(mod.getPrefs().getValue(GameModule.REAL_NAME) + " > Game State Checksum = OK", false);
				}
			} else {
				WriteLine(mod.getPrefs().getValue(GameModule.REAL_NAME)
						+ " > Game State Checksum = FAILED -- Synchronisation possibly lost, recommended to resync the game",
						false);
			}
		}
	};

	@Override
	protected void executeCommand() {
		commandDelay.cancel();
		commandDelay = new java.util.Timer();
		// we need a delay, otherwise the checksum may be calculated on a gamestate
		// where End Turn isn't fully completed
		commandDelay.schedule(executeCommandDelayListener, 500);
	}

	@Override
	protected Command myUndoCommand() {
		return null;
	}

	public static String calculateChecksum() {
		final GameModule mod = GameModule.getGameModule();
		final GameState state = mod.getGameState();
		final Collection<GamePiece> piecesCollection = state.getAllPieces();

		Iterator<GamePiece> iterator = piecesCollection.iterator();
		List<GamePiece> pieces = new ArrayList<GamePiece>(piecesCollection.size());
		while (iterator.hasNext()) {
			GamePiece p = iterator.next();

			// DEBUG
//			if (p.getType().contains("stack"))
//			{
//				WriteLine("found a stack", false);
//			}

			if (p.getName().trim().length() > 0 && !p.getType().equals("stack"))
				pieces.add(p);
		}

		Collections.sort(pieces, new Comparator<GamePiece>() {
			public int compare(GamePiece p1, GamePiece p2) {
				return p1.getId().compareTo(p2.getId());
			}
		});

		StringBuilder data = new StringBuilder();
		// DEBUG
//		StringBuilder debugdata = new StringBuilder();

		for (int i = 0; i < pieces.size(); i++) {
			GamePiece piece = pieces.get(i);

			// data.append(piece.getId()).append(';').append(piece.getName()).append(';').append(piece.getState()).append('%');
			// .append(piece.getMap().getId()).append(';').append(piece.getPosition().getX()).append(';').append(piece.getPosition().getY()).append('#');

			String[] statePieces = piece.getState().split("\\t");
			String place = statePieces[statePieces.length - 1];
			data.append(piece.getId()).append(';').append(place).append('%');

			// DEBUG
//			debugdata.append(piece.getId()).append(';').append(piece.getName()).append(';').append(piece.getType()).append(';').append(piece.getState()).append('%');
		}

		// DEBUG
//		//WriteLine(data.toString().replaceAll("%", "\n"), false);
//		WriteLine("DEBUGDATA1", false);
//		
//		WriteLine(debugdata.toString(), false);
//		
//		WriteLine("DEBUGDATA2", false);
//		
//		WriteLine(data.toString(), false);

		String checksum = "";

		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");

			byte[] encodedhash = digest.digest(data.toString().getBytes(StandardCharsets.UTF_8));

			checksum = bytesToHex(encodedhash);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			WriteLine("- ERROR: Failed to get hash algorithm", false);
			checksum = "";
		}

		// DEBUG
//		WriteLine("checksum = " + checksum, false);

		return checksum;
	}

	private static String bytesToHex(byte[] hash) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

	static void WriteLine(String msgLine, boolean logMsg) {
		final GameModule mod = GameModule.getGameModule();

		FormattedString cStr = new FormattedString("- " + msgLine);
		final Command cc = new Chatter.DisplayText(mod.getChatter(), cStr.getLocalizedText(cStr, msgLine));
		cc.execute();
		if (logMsg)
			mod.sendAndLog(cc);
		else
			mod.getServer().sendToOthers(cc);
	}
}
