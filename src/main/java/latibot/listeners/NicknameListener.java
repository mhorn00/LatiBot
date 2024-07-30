package latibot.listeners;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import latibot.LatiBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class NicknameListener extends ListenerAdapter {

	private static final Path nicknamePath = Paths.get("nicknames.json");
	public static HashMap<String, NicknameHistory> nicknamesHistory;
	public static Map<String, NicknameCmdInfo> hashes = new HashMap<String, NicknameCmdInfo>();

	@Override
	public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
		// ignore if nickname is null or blank
		if (event.getNewNickname() == null || event.getNewNickname().isBlank()) return;
		// populate nicknamesHistory from nicknames.json if null
		if (nicknamesHistory == null) populateHistory(event.getJDA());
		// add updated nickname
		if (!nicknamesHistory.containsKey(event.getMember().getId())) nicknamesHistory.put(event.getEntity().getId(), new NicknameHistory(event.getMember()));
		NicknameCmdInfo info = null;
		if (!hashes.isEmpty()) {
			String hash = hashNameChange(event.getOldNickname(), event.getNewNickname(), event.getEntity().getId());
			if (hashes.containsKey(hash)) {
				info = hashes.get(hash);
				if (info.needsConfirm) {
					info.confirm();
					return;
				}
				nicknamesHistory.get(event.getEntity().getId()).addNickname(event.getNewNickname(), info.agressorId);
				writeJson();
				return;
			}
		}
		LatiBot.LOG.info("Recived nickname change event with no hash! Assuming self change.");
		nicknamesHistory.get(event.getEntity().getId()).addNickname(event.getNewNickname(), event.getMember().getId());
		writeJson();
	}

	public static void populateHistory(JDA jda) {
		// make sure nicknames.json exists
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
			if (str.isEmpty())
				return;
			json = new JSONObject(str);
		} catch (IOException e) {
			LatiBot.LOG.error("Error reading nicknames.json!", e);
			return;
		}
		for (Guild g : jda.getGuilds()) {
			if (json.has(g.getId())) {
				JSONArray members = json.getJSONArray(g.getId());
				for (int i = 0; i < members.length(); i++) {
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

	public static String hashNameChange(String oldNickname, String newNickname, String id) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] encode = md.digest((oldNickname + newNickname + id).getBytes(StandardCharsets.UTF_8));
			return IntStream.range(0, encode.length).mapToObj(i -> String.format("%02x", encode[i] & 0xff)).collect(Collectors.joining()).substring(0, encode.length >= 95 ? 95 : encode.length);
		} catch (NoSuchAlgorithmException e) {
			LatiBot.LOG.error("Error hashing name change! Using fallback, errors may occure!", e);
			// backup "hash"
			String backup = oldNickname + newNickname + id;
			return backup.substring(0, backup.length() >= 95 ? 95 : backup.length());
		}
	}

	public static void addHash(String hash, NicknameCmdInfo info) {
		hashes.put(hash, info);
	}

	public static boolean confirmHash(String hash) {
		if (hashes.containsKey(hash)) {
			NicknameCmdInfo info = hashes.get(hash);
			if (info.needsConfirm && info.confirmed) {
				nicknamesHistory.get(info.victimId).addNickname(info.newNickname, info.agressorId);
				writeJson();
				return true;
			}
		}
		LatiBot.LOG.error("Error confirming hash! Hash not found or not confirmed! Hash: " + hash);
		return false;
	}

	public static final class NicknameCmdInfo {
		public final String oldNickname;
		public final String newNickname;
		public final String victimId;
		public final String agressorId;
		public final String hash;
		public final boolean needsConfirm;
		public boolean confirmed;
		public String msgId;

		public NicknameCmdInfo(String oldNickname, String newNickname, String victimId, String agressorId, String hash) {
			this.oldNickname = oldNickname;
			this.newNickname = newNickname;
			this.victimId = victimId;
			this.agressorId = agressorId;
			this.hash = hash;
			this.needsConfirm = false;
			this.msgId = null;
			this.confirmed = false;
		}
		
		public NicknameCmdInfo(String oldNickname, String newNickname, String victimId, String agressorId, String hash, String msgId) {
			this.oldNickname = oldNickname;
			this.newNickname = newNickname;
			this.victimId = victimId;
			this.agressorId = agressorId;
			this.hash = hash;
			this.needsConfirm = true;
			this.msgId = msgId;
			this.confirmed = false;
		}

		public void confirm() {
			confirmed = true;
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

		public void addNickname(String newNickname, String changedById) {
			nicknames.add(new NicknameEntry(newNickname, Date.from(Instant.now()), changedById));
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

		public NicknameEntry getLatestNickname() {
			return nicknames.get(nicknames.size() - 1);
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
			for (int i = 0; i < names.length(); i++)
				nn.add(NicknameEntry.fromJson(names.getJSONObject(i)));
			return new NicknameHistory(g, m, nn);
		}

		public static class NicknameEntry {
			public String nickname;
			public Date date;
			public String changedById;

			public NicknameEntry(String nickname, Date date, String changedById) {
				this.nickname = nickname;
				this.date = date;
				this.changedById = changedById;
			}

			public JSONObject toJson() {
				JSONObject json = new JSONObject();
				json.put("nickname", nickname);
				json.put("changedById", changedById);
				json.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
				return json;
			}

			public static NicknameEntry fromJson(JSONObject json) {
				try {
					return new NicknameEntry(json.getString("nickname"),
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(json.getString("datetime")), json.getString("changedById"));
				} catch (JSONException | ParseException e) {
					LatiBot.LOG.error("Exception parsing NicknameEntry json!", e);
					return null;
				}
			}
		}
	}
}
