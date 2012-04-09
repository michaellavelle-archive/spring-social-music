package com.springsocialmusic.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.social.ResourceNotFoundException;
import org.springframework.social.exfm.api.ExFm;
import org.springframework.social.exfm.api.impl.ExFmTemplate;
import org.springframework.social.soundcloud.api.SoundCloud;
import org.springframework.social.soundcloud.api.Track;
import org.springframework.social.soundcloud.api.impl.SoundCloudTemplate;

public class SoundCloudToExFmMigrator {

	private String soundCloudApiKey;
	private int soundCloudFetchPageSize = 50;
	
	public SoundCloudToExFmMigrator(String soundCloudApiKey)
	{
		this.soundCloudApiKey = soundCloudApiKey;
	}
	
	private void sleepForLimitRate()
	{
		try
		{
			Thread.sleep(200);
		}
		catch (Exception e) {}
	}
	
	public void loveSoundCloudFavoritesOnExFm(String soundCloudUserId,String exFmUserId,String exFmPassword)
	{		
		// Construct a new SoundCloudTemplate only authorized by api key, as user-level authorization is not needed
		// to retrieve favorite tracks
		SoundCloud soundCloud = new SoundCloudTemplate(soundCloudApiKey);
		
		// Construct a new ExFmTemplate authorized for a specific user, specifying api base url, username and password
		ExFm exFm = new ExFmTemplate("http://ex.fm/api/v3",exFmUserId,exFmPassword);
		
		// Retrieve the first page of SoundCloud favorite tracks
		Page<Track> soundCloudFavorites = soundCloud.usersOperations().userOperations(soundCloudUserId).getFavorites(new PageRequest(0,soundCloudFetchPageSize));
		
		// For each page of SoundCloud favorite tracks...
		for (int pageNumber = 0; pageNumber < soundCloudFavorites.getTotalPages();pageNumber++)
		{
			// For each SoundCloud favorite track on current page, love the track on Ex.Fm using the stream url
			for (Track soundCloudTrack : soundCloudFavorites)
			{
				sleepForLimitRate();
				try
				{
					exFm.songOperations().loveSongBySourceUrl(soundCloudTrack.getStreamUrl());
				}
				catch (ResourceNotFoundException e)
				{
					// Failed to love song as Ex.Fm can't find information about the soundcloud track with this url
				}
			}
			// Get next page of tracks from SoundCloud if available
			if (soundCloudFavorites.hasNextPage()) soundCloudFavorites = soundCloud.usersOperations().userOperations(soundCloudUserId).getFavorites(new PageRequest(pageNumber + 1,soundCloudFetchPageSize));
		}	
	}
	
}
