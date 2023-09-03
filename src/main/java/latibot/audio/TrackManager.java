package latibot.audio;

import java.util.Collections;
import java.util.LinkedList;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import latibot.LatiBot;
import net.dv8tion.jda.api.entities.Member;

public class TrackManager extends AudioEventAdapter {

	private final AudioPlayer audioPlayer;
	private final SongQueue queue;
	private boolean shouldRepeat = false;
		
	public TrackManager(final AudioPlayer ap) {
		this.audioPlayer = ap;
		this.queue = new SongQueue();
	}
	
	public void queue(final AudioTrack track, final Member member) {
		queue.add(new AudioTrackInfo(track, member));
		if (audioPlayer.getPlayingTrack() == null) {
			audioPlayer.playTrack(track);
		}
	}
	
	public void queueNow(final AudioTrack track, final Member member) {
		queue.addFirst(new AudioTrackInfo(track, member));
		if (audioPlayer.getPlayingTrack() == null) {
			audioPlayer.playTrack(track);
		} else {
			skip();
		}
	}
	
	public void queueNext(final AudioTrack track, final Member member) {
		queue.addFirst(new AudioTrackInfo(track, member));
		if (audioPlayer.getPlayingTrack() == null) {
			audioPlayer.playTrack(track);
		}
	}
	
	public boolean togglePause() {
		boolean paused = !audioPlayer.isPaused();
		audioPlayer.setPaused(paused);
		return paused;
	}
	
	public void clearQueue() {
		queue.clear();
	}
	
	public void skip() {
		audioPlayer.stopTrack();
	}
	
	public boolean toggleRepeat() {
		return this.shouldRepeat = !this.shouldRepeat;
	}
	
	public SongQueue getQueue() {
		return queue;
	}
	
	public void shuffleQueue() {
		queue.shuffleQueue();
	}
	
	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason == AudioTrackEndReason.FINISHED || endReason == AudioTrackEndReason.LOAD_FAILED || endReason == AudioTrackEndReason.STOPPED) {
			if (shouldRepeat) {
				audioPlayer.playTrack(queue.getCurrent().getAudioTrack().makeClone());
			} else {
				AudioTrackInfo next = queue.next();
				if (next != null) {
					audioPlayer.playTrack(next.getAudioTrack());	
				}
			}
		}
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		LatiBot.LOG.warn("Excetion occured while playing track "+track.getInfo().title+": ",exception);
	}

	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		LatiBot.LOG.warn("Got stuck playing track "+track.getInfo().title + " ("+thresholdMs+" ms)");
	}
	
	public static class SongQueue {
		private LinkedList<AudioTrackInfo> queue;
		private AudioTrackInfo currentTrack;
		
		public SongQueue() {
			queue = new LinkedList<AudioTrackInfo>();
		}
		
		public void add(AudioTrackInfo track) {
			if (currentTrack == null) {
				currentTrack = track;
			} else {
				queue.addLast(track);	
			}
		}
		
		public void addFirst(AudioTrackInfo track) {
			if (currentTrack == null) {
				currentTrack = track;
			} else {
				queue.addFirst(track);	
			}
		}

		public AudioTrackInfo next() {
			currentTrack = queue.pollFirst();
			return currentTrack;
		}
		
		public AudioTrackInfo getCurrent() {
			return currentTrack;
		}
		
		public void clear() {
			queue.clear();
		}
		
		public boolean isQueueEmpty() {
			return queue.isEmpty();
		}
		
		public void shuffleQueue() {
			Collections.shuffle(queue);
		}

		public AudioTrackInfo get(int i) {
			return queue.get(i);
		}
		
		public int size() {
			return queue.size();
		}
	}
}
