package com.general_hello.bot.database.Airtable;

import com.general_hello.Config;
import com.general_hello.bot.database.DataUtils;
import com.general_hello.bot.utils.EODUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class Airtable {
    public static final String BASE_ID = Config.get("base_id");
    public static final String EOD_BASE_ID = Config.get("eod_base_id");
    public static final String API_KEY = Config.get("airtable");
    public static final String TRF_USERS_TABLE_ID = Config.get("trf_users_table");
    public static final String TRF_EOD_USERS_TABLE_ID = Config.get("TRF_EOD_USERS".toLowerCase());
    public static final String EOD_OVERVIEW = Config.get("eod_overview_table");
    public static final String EOD_LOG = Config.get("eod_log_table");
    public static final String EOD_Q1 = Config.get("eod_q1_table");
    public static final String EOD_Q2 = Config.get("eod_q2_table");
    public static final String EOD_Q3 = Config.get("eod_q3_table");
    public static final String TRF_AG_LEADERS_TABLE_ID = Config.get("trf_ag_table");

    /*public static void smth() {
        String apiUrl = "https://api.airtable.com/v0/" + EOD_BASE_ID + "/" + EOD_LOG + "?offset=" + "itrjqcADxl3sG58JY/recniuXICy5a286oi";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + API_KEY)
                .get()
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            String responseBody = response.body().string();
            System.out.println(responseBody);
            JSONObject json = new JSONObject(responseBody);
            JSONArray records = json.getJSONArray("records");
            System.out.println("Found " + records.length() + " records");
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                JSONObject field = record.getJSONObject("fields");
                String discordUsername = field.getJSONArray("DiscordUsername").getString(0);
                List<User> users = Bot.getJda().getUsersByName(discordUsername, true);
                if (users.isEmpty()) continue;
                User user = users.get(0);
                long userIdLong = user.getIdLong();
                DataUtils.setReportStreak(userIdLong, field.getInt("Current EOD Streak (Turn into script formula)"));
                DataUtils.setBestReportStreak(userIdLong, field.getInt("Best EOD Streak (Turn into formula)"));
                DataUtils.setMissedDays(userIdLong, field.getInt("Missed Days (Turn into script formula)"));
                System.out.println("Done " + discordUsername);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("ALL DONE");
    }*/
    public static void update(User user, String date) {
        date = date.split("T")[0];
        try {
            String recordIdOfUser = getRecordIdInEODReportFromEODUser(user.getName().replace(" ", ""));
            //For EOD Log
            int reportStreak = DataUtils.getReportStreak(user.getIdLong());
            int bestReportStreak = DataUtils.getBestReportStreak(user.getIdLong());
            int missedDays = DataUtils.getMissedDays(user.getIdLong());
            String overviewId = DataUtils.getOverviewId(user.getIdLong());
            OkHttpClient client = new OkHttpClient();
            System.out.println(overviewId);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create("{\"name\":\"" + date + "\", \"options\": {\"color\": \"greenBright\", \"icon\": \"check\"}, \"type\":\"checkbox\"}", mediaType);
            Request request = new Request.Builder()
                    .url("https://api.airtable.com/v0/meta/bases/" + EOD_BASE_ID + "/tables/" + EOD_LOG + "/fields")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try {
                System.out.println(client.newCall(request).execute().body().string());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            client = new OkHttpClient();
            mediaType = MediaType.parse("application/json");
            String trueOrFalse = "true";
            if (!date.split("T")[0].equals(OffsetDateTime.now(ZoneId.of("UTC-6")).toString().split("T")[0])) {
                trueOrFalse = "false";
            }

            body = RequestBody.create("{\"fields\": {\"TRF 2.0 Users\": [\"" + recordIdOfUser + "\"], \"EOD Overview\": [\"" + overviewId + "\"], \"Current EOD Streak (Turn into script formula)\": " + reportStreak + ", \"Best EOD Streak (Turn into formula)\": " + bestReportStreak + ", \"Missed Days (Turn into script formula)\": " + missedDays + ", \"" + date + "\": " + trueOrFalse + "}}", mediaType);
            request = new Request.Builder()
                    .url("https://api.airtable.com/v0/" + EOD_BASE_ID + "/" + EOD_LOG + "/" + getRecordIdFromUserRecordId(user.getName(), EOD_LOG))
                    .patch(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();
            System.out.println(client.newCall(request).execute().body().string());
            // For Q1
            int relapseFreeStreak = DataUtils.getRelapseFreeStreak(user.getIdLong());
            int bestRelapseFreeStreak = DataUtils.getBestRelapseFreeStreak(user.getIdLong());
            int relapseCount = DataUtils.getRelapseCount(user.getIdLong());

            client = new OkHttpClient();

            mediaType = MediaType.parse("application/json");
            body = RequestBody.create("{\"name\":\"" + date + "\", \"options\": {\"color\": \"greenBright\", \"icon\": \"check\"}, \"type\":\"checkbox\"}", mediaType);
            request = new Request.Builder()
                    .url("https://api.airtable.com/v0/meta/bases/" + EOD_BASE_ID + "/tables/" + EOD_Q1 + "/fields")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try {
                System.out.println(client.newCall(request).execute().body().string());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            client = new OkHttpClient();
            mediaType = MediaType.parse("application/json");
            body = RequestBody.create("{\"fields\": {\"TRF 2.0 Users\": [\"" + recordIdOfUser + "\"], \"EOD Overview\": [\"" + overviewId + "\"], \"Current Streak (Turn into script formula)\": " + relapseFreeStreak + ", \"Best Streak (Turn into script formula)\": " + bestRelapseFreeStreak + ", \"Relapse Counter (Turn into script formula)\": " + relapseCount + ", \"" + date + "\": " + (relapseFreeStreak == 0 ? "true" : "false") + "}}", mediaType);
            request = new Request.Builder()
                    .url("https://api.airtable.com/v0/" + EOD_BASE_ID + "/" + EOD_Q1 + "/" + getRecordIdFromUserRecordId(user.getName(), EOD_Q1))
                    .patch(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();
            System.out.println(client.newCall(request).execute().body().string());
            // For Q2
            String additionalInsertion = "";
            String color = DataUtils.getColor(user.getIdLong());
            switch (color) {
                case "Red" -> {
                    additionalInsertion = ", \"Red Counter (To be scripted)\": " + (DataUtils.getColorRedCount(user.getIdLong()) + 1);
                    DataUtils.incrementColorRedCount(user.getIdLong());
                }
                case "Yellow" -> {
                    additionalInsertion = ", \"Yellow Counter (To be scripted)\": " + (DataUtils.getColorYellowCount(user.getIdLong()) + 1);
                    DataUtils.incrementColorYellowCount(user.getIdLong());
                }
                case "Green" -> {
                    additionalInsertion = ", \"Green Counter (To be scripted)\": " + (DataUtils.getColorGreenCount(user.getIdLong()) + 1);
                    DataUtils.incrementColorGreenCount(user.getIdLong());
                }
            }

            client = new OkHttpClient();

            mediaType = MediaType.parse("application/json");
            body = RequestBody.create("{\"name\":\"" + date + "\", \"options\": {\"choices\": [{\"color\": \"redLight1\", \"name\": \"Red\"}, {\"color\": \"yellowLight1\", \"name\": \"Yellow\"}, {\"color\": \"greenLight2\", \"name\": \"Green\"}]}, \"type\":\"singleSelect\"}", mediaType);
            request = new Request.Builder()
                    .url("https://api.airtable.com/v0/meta/bases/" + EOD_BASE_ID + "/tables/" + EOD_Q2 + "/fields")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try {
                System.out.println(client.newCall(request).execute().body().string());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            client = new OkHttpClient();
            mediaType = MediaType.parse("application/json");
            body = RequestBody.create("{\"fields\": {\"TRF 2.0 Users\": [\"" + recordIdOfUser + "\"]" + additionalInsertion + ", \"EOD Overview\": [\"" + overviewId + "\"], \"Current Color (To be scripted)\": \"" + color + "\", \"" + date + "\":  \"" + color + "\"}}", mediaType);
            request = new Request.Builder()
                    .url("https://api.airtable.com/v0/" + EOD_BASE_ID + "/" + EOD_Q2 + "/" + getRecordIdFromUserRecordId(user.getName(), EOD_Q2))
                    .patch(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();
            System.out.println(client.newCall(request).execute().body().string());
            // For Q3
            int urge = DataUtils.getUrge(user.getIdLong());
            double totalUrge = DataUtils.getTotalUrge(user.getIdLong());
            double totalAnswered = DataUtils.getTotalAnswered(user.getIdLong());
            double averageUrge = totalUrge / totalAnswered;
            System.out.println("Average urge: " + averageUrge);

            client = new OkHttpClient();

            mediaType = MediaType.parse("application/json");
            body = RequestBody.create("{\"name\":\"" + date + "\", \"options\": {\"choices\": [{\"name\": \"1\"}, {\"name\": \"2\"}, {\"name\": \"3\"}, {\"name\": \"4\"}, {\"name\": \"5\"}, {\"name\": \"6\"}, {\"name\": \"7\"}, {\"name\": \"8\"}, {\"name\": \"9\"}, {\"name\": \"10\"}]}, \"type\":\"singleSelect\"}", mediaType);
            request = new Request.Builder()
                    .url("https://api.airtable.com/v0/meta/bases/" + EOD_BASE_ID + "/tables/" + EOD_Q3 + "/fields")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try {
                System.out.println(client.newCall(request).execute().body().string());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            client = new OkHttpClient();
            mediaType = MediaType.parse("application/json");
            body = RequestBody.create("{\"fields\": {\"TRF 2.0 Users\": [\"" + recordIdOfUser + "\"], \"Current Score\": " + urge + ", \"EOD Overview\": [\"" + overviewId + "\"], \"Average (To be scripted)\": " + averageUrge + ", \"" + date + "\":  \"" + urge + "\"}}", mediaType);
            request = new Request.Builder()
                    .url("https://api.airtable.com/v0/" + EOD_BASE_ID + "/" + EOD_Q3 + "/" + getRecordIdFromUserRecordId(user.getName(), EOD_Q3))
                    .patch(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();
            System.out.println(client.newCall(request).execute().body().string());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String getRecordIdInEODReportFromEODUser(String discordtag) throws IOException {
        // Airtable for AG
        System.out.println(discordtag + " Discord tag");
        String apiUrl = "https://api.airtable.com/v0/" + EOD_BASE_ID + "/" + TRF_EOD_USERS_TABLE_ID + "?filterByFormula=DiscordName=\"" + discordtag + "\"";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + API_KEY)
                .get()
                .build();
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        System.out.println(responseBody);
        JSONObject json = new JSONObject(responseBody);
        JSONArray records = json.getJSONArray("records");
        if (records.length() > 0) {
            JSONObject firstRecord = records.getJSONObject(0);
            return firstRecord.getString("id");
        }
        return "";
    }

    public static String getRecordIdInEODOverview(String discordtag) throws IOException {
        // Airtable for AG
        System.out.println(discordtag + " Discord tag");
        String apiUrl = "https://api.airtable.com/v0/" + EOD_BASE_ID + "/" + EOD_OVERVIEW + "?filterByFormula=DiscordName=\"" + discordtag + "\"";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + API_KEY)
                .get()
                .build();
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        System.out.println(responseBody);
        JSONObject json = new JSONObject(responseBody);
        JSONArray records = json.getJSONArray("records");
        if (records.length() > 0) {
            JSONObject firstRecord = records.getJSONObject(0);
            return firstRecord.getString("id");
        }
        return "";
    }

    public static String getRecordIdFromUserRecordId(String discordName, String table) throws IOException {
        // Airtable for AG
        discordName = discordName.replace(" ", "");
        String apiUrl = "https://api.airtable.com/v0/" + EOD_BASE_ID + "/" + table + "?filterByFormula=DiscordUsername=\"" + discordName + "\"";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + API_KEY)
                .get()
                .build();
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();

        JSONObject json = new JSONObject(responseBody);
        JSONArray records = json.getJSONArray("records");
        if (records.length() > 0) {
            JSONObject firstRecord = records.getJSONObject(0);
            return firstRecord.getString("id");
        }
        return null;
    }

    public static void runEodReportCleanup(String value, GenericComponentInteractionCreateEvent event, Member member, User author, String[] ids) {
        // EOD Report
        int rating = Integer.parseInt(value);
        DataUtils.setLastAnsweredTime(author.getIdLong(), ids[3]);
        int relapse = DataUtils.getRelapse(member.getIdLong());
        boolean didRelapse = Boolean.TRUE.equals(DataUtils.getBooleanFromInt(relapse));
        Guild guild = event.getJDA().getGuildById("1062100366105268327");

        if (!EODUtil.newUsers.contains(member.getIdLong())) {
            // Discord database stuff
            DataUtils.setIsEODAnswered(member.getIdLong(), true);
            DataUtils.incrementReportStreak(member.getIdLong());
            if (!didRelapse) {
                DataUtils.incrementRelapseFreeStreak(member.getIdLong());
                int relapseFreeStreak = DataUtils.getRelapseFreeStreak(member.getIdLong());
                if (relapseFreeStreak > DataUtils.getBestRelapseFreeStreak(member.getIdLong())) {
                    DataUtils.setBestRelapseFreeStreak(member.getIdLong(), relapseFreeStreak);
                }

                if (relapseFreeStreak == 1) {
                    guild.addRoleToMember(member, guild.getRoleById("1071181504727765103")).queue();
                }
            } else {
                DataUtils.setRelapseFreeStreak(member.getIdLong(), 0);
            }

            int reportStreak = DataUtils.getReportStreak(member.getIdLong());
            if (reportStreak > DataUtils.getBestReportStreak(member.getIdLong())) {
                DataUtils.setBestReportStreak(member.getIdLong(), reportStreak);
            }

            DataUtils.incrementTotalAnswered(member.getIdLong());
            DataUtils.incrementTotalUrge(member.getIdLong(), rating);

            // Airtable stuff ????
            System.out.println("Updating airtable");
            Airtable.update(event.getUser(), ids[3]);

            if (reportStreak == 1) {
                guild.addRoleToMember(member, guild.getRoleById("1071181482003021954")).queue();
            }

            if (reportStreak == 14) {
                guild.addRoleToMember(member, guild.getRoleById("1071181566103003297")).queue();
            }

            int relapseFreeStreak = DataUtils.getRelapseFreeStreak(member.getIdLong());
            if (relapseFreeStreak == 1) {
                guild.addRoleToMember(member, guild.getRoleById("1071181504727765103")).queue();
            }

            if (relapseFreeStreak == 14) {
                guild.addRoleToMember(member, guild.getRoleById("1071181601419038850")).queue();
            }

            DataUtils.setDidAnswer(member.getIdLong(), true);

            if (EODUtil.secondTimeUsers.contains(member.getIdLong())) {
                System.out.println("Second time users");
                if (didRelapse) {
                    guild.addRoleToMember(member, guild.getRoleById(1071182182862823455L)).queue();
                }
                EODUtil.secondTimeUsers.remove(member.getIdLong());
            }
            return;
        }

        EODUtil.newUsers.remove(member.getIdLong());

        System.out.println("First time users");
        if (didRelapse) {
            System.out.println("First time users did relapse");
            guild.addRoleToMember(member, guild.getRoleById(1071160346640912384L)).queue();
        }
        EODUtil.secondTimeUsers.add(member.getIdLong());
    }
}
