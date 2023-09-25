package latibot.listeners;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import latibot.LatiBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class NicknameListener extends ListenerAdapter{

	private static final Path nicknamePath = Paths.get("nicknames.json");
	public static HashMap<String, NicknameHistory> nicknamesHistory;
	
	@Override
	public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
		//populate nicknamesHistory from nicknames.json if null
		if (nicknamesHistory == null) populateHistory(event.getJDA());
		//add updated nickname
		if (!nicknamesHistory.containsKey(event.getMember().getId())) {
			nicknamesHistory.put(event.getEntity().getId(), new NicknameHistory(event.getMember()));
		}
		nicknamesHistory.get(event.getEntity().getId()).addNickname(event.getNewNickname());
		writeJson();
	}
	
	public static void populateHistory(JDA jda) {
		//make sure nicknames.json exists
		if (!Files.exists(nicknamePath)) {
			try {
				Files.createFile(nicknamePath);
			} catch (IOException e) {
				LatiBot.LOG.error("Error creating nicknames.json!", e);
				return;
			}
		}
		nicknamesHistory = new HashMap<String, NicknameHistory>();
		JSONObject json;
		try {
			String str = new String(Files.readAllBytes(nicknamePath));
			if (str.isEmpty()) return;
			json = new JSONObject(str);
		} catch (IOException e) {
			LatiBot.LOG.error("Error reading nicknames.json!", e);
			return;
		}
		for (Guild g : jda.getGuilds()) {
			if (json.has(g.getId())) {
				JSONArray members = json.getJSONArray(g.getId());
				for (int i = 0; i<members.length(); i++) {
					JSONObject member = members.getJSONObject(i);
					NicknameHistory h = NicknameHistory.fromJson(member);
					nicknamesHistory.put(member.getJSONObject("member").getString("id"), h);
				}
			}
		}
	}
	
	public static void writeJson() {
		JSONObject json = new JSONObject();
		for (NicknameHistory nh : nicknamesHistory.values()) {
			if (!json.has(nh.getGuild().getId())) {
				json.put(nh.getGuild().getId(), new JSONArray());
			}
			json.getJSONArray(nh.getGuild().getId()).put(nh.toJson());
		}
		try {
			Files.write(nicknamePath, json.toString().getBytes());
			LatiBot.LOG.info("Wrote nicknames.json");
		} catch (IOException e) {
			LatiBot.LOG.error("Error reading nicknames.json!", e);
			return;
		}
	}
	
	public static class NicknameHistory {
		private Guild guild;
		private Member member;
		private List<NicknameEntry> nicknames;
		
		public NicknameHistory(Member member) {
			this(member.getGuild(), member, new ArrayList<NicknameEntry>());
		}
		
		public NicknameHistory(Guild guild, Member member, List<NicknameEntry> nicknames) {
			this.guild = guild;
			this.member = member;
			this.nicknames = nicknames;
		}
		
		public void addNickname(String newNickname) {
			nicknames.add(new NicknameEntry(newNickname, Date.from(Instant.now())));
		}
		
		public Guild getGuild() {
			return guild;
		}
		
		public Member getMember() {
			return member;
		}

		public List<NicknameEntry> getNicknames() {
			return nicknames;
		}

		public JSONObject toJson() {
			JSONObject json = new JSONObject();
			JSONObject user = new JSONObject();
			JSONArray names = new JSONArray();
			user.put("id", member.getId());
			user.put("username", member.getUser().getName());
			nicknames.forEach(v -> names.put(v.toJson()));
			json.put("guild", guild.getId());
			json.put("member", user);
			json.put("nicknames", names);
			return json;
		}
		
		public static NicknameHistory fromJson(JSONObject json) {
			JSONArray names = json.getJSONArray("nicknames");
			Guild g = LatiBot.jdaInst.getGuildById(json.getString("guild"));
			Member m = g.getMemberById(json.getJSONObject("member").getString("id"));
			ArrayList<NicknameEntry> nn = new ArrayList<NicknameEntry>();
			for (int i = 0; i<names.length();i++) nn.add(NicknameEntry.fromJson(names.getJSONObject(i)));
			return new NicknameHistory(g, m, nn);
		}
		
		public static class NicknameEntry {
			public String nickname;
			public Date date;
			
			public NicknameEntry(String nickname, Date date) {
				this.nickname = nickname;
				this.date = date;
			}
			
			public JSONObject toJson() {
				JSONObject json = new JSONObject();
				json.put("nickname", nickname);
				json.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
				return json;
			}
			
			public static NicknameEntry fromJson(JSONObject json) {
				try {
					return new NicknameEntry(json.getString("nickname"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(json.getString("datetime")));
				} catch (JSONException | ParseException e) {
					LatiBot.LOG.error("Exception parsing NicknameEntry json!", e);
					return null;
				}
			}
		}
	}
}
