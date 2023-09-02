package latibot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Member;

public class AudioTrackInfo {
	
	private final AudioTrack track;
	private final Member member;
	
	public AudioTrackInfo(final AudioTrack at, final Member member) {
		this.track = at;
		this.member = member;
	}
	
	public AudioTrack getAudioTrack() {
		return track;
	}
	
	public Member getMember() {
		return member;
	}
}
