/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.metaPlayer;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.utils.Record;

import java.util.ArrayList;
import java.util.List;


public class SongCtrl {

	private ConfigFile songConfig;

	private List<Song> songs;


	public SongCtrl() throws JsonParseException {

		songConfig = new ConfigFile("songs", true);

		// create a default config file, if necessary
		if (songConfig.getAllContents().isEmpty()) {
			songConfig.setAllContents(new JSON("[]"));
		}

		JSON songRecordContainer = songConfig.getAllContents();
		List<Record> songRecords = songRecordContainer.getValues();

		songs = new ArrayList<>();

		for (Record record : songRecords) {
			Song song = new Song(record);
			songs.add(song);
		}
	}

	public List<Song> getSongs() {
		return songs;
	}

	public Record getSongData() {

		JSON result = new JSON();
		result.makeArray();

		for (Song song : songs) {
			result.append(song.toRecord());
		}

		return result;
	}

	public void save() {
		songConfig.setAllContents(getSongData());
	}
}
