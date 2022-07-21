package com.general_hello.bot.commands;

import com.general_hello.Bot;
import com.general_hello.bot.ButtonPaginator;
import com.general_hello.bot.database.DataUtils;
import com.general_hello.bot.objects.GlobalVariables;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LeaderboardCommand extends SlashCommand {
    public LeaderboardCommand() {
        this.name = "leaderboard";
        this.help = "Shows the leaderboard";
        this.userPermissions = new Permission[]{
                Permission.MANAGE_SERVER,
                Permission.MESSAGE_MANAGE
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        List<Long> userIds = DataUtils.getPredictionUsers();
        if (userIds == null) {
            event.reply("No users have predicted yet!").setEphemeral(true).queue();
            return;
        } else {
            event.reply("Loading the leaderboard...").setEphemeral(true).queue();
        }

        ArrayList<String> pages = new ArrayList<>();
        List<Predictor> predictors = new ArrayList<>();
        for(long userId : userIds) {
            int winCount = DataUtils.getWinCount(userId);
            int loseCount = DataUtils.getLoseCount(userId);
            predictors.add(new Predictor(event.getJDA().getUserById(userId).getAsTag(), winCount, loseCount, userId));
        }

        // Sorting the predictors by win count
        Collections.sort(predictors);

        for (Predictor predictor : predictors) {
            pages.add(predictor.toString());
        }

        if (pages.isEmpty()) {
            return;
        }

        ButtonPaginator.Builder builder = new ButtonPaginator.Builder(event.getJDA());
        builder.setColor(event.getGuild().getSelfMember().getColor());
        builder.setItemsPerPage(7);
        builder.setFooter("Press the buttons below to navigate between the leaderboard");
        builder.setEventWaiter(Bot.getBot().getEventWaiter());
        builder.setTitle("Prediction Leaderboard");
        builder.setTimeout(365, TimeUnit.DAYS);
        builder.setItems(pages);
        builder.build().paginate(event.getChannel().asTextChannel(), 1);
    }

    private record Predictor(String userName, int wins, int loss, long userId) implements Comparable<Predictor> {

        public int getWins() {
            return wins;
        }

        public int getLoss() {
            return loss;
        }

        @Override
        public int compareTo(@NotNull LeaderboardCommand.Predictor o) {
            int scoreA = this.wins - this.loss;
            int scoreB = o.getWins() - o.getLoss();

            if (scoreA > scoreB) {
                return -1;
            } else if (scoreA < scoreB) {
                return 1;
            } else {
                return Integer.compare(o.wins, this.wins);
            }
        }

        @Override
        public String toString() {
            return "**" + userName + "** - " +
                    wins + " **W** " + loss + " **L** " +
                    DataUtils.getLastResults(userId).replaceAll("W", GlobalVariables.WIN_EMOJI)
                            .replaceAll("L", GlobalVariables.LOSE_EMOJI);
        }
    }
}