package services;

import entities.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestionsGenerator {
    private static final int NUMBER_ANSWERS = 3;
    private static final List<League> LEAGUE_CODES =
            List.of(
                    new League("1. Bundesliga", "BL1"),
                    new League("Premiere League", "PL"),
                    new League("Serie A", "SA"),
                    new League("Primera Division", "PD"),
                    new League("Ligue 1", "FL1"),
                    new League("Champions-League", "CL")
            );

    public static FootballPlayer fetchPlayer(final String json, final int number) {
        //System.out.println(json);
        final Pattern pattern = Pattern.compile("\\[.*\\]");
        final Matcher matcher = pattern.matcher(json);
        String scorers = "";
        while (matcher.find()) {
            scorers = json.substring(matcher.start() + 1, matcher.end() - 1);
        }
//        System.out.println(scorers);

        final String[] players = scorers.split("player");
//        System.out.println("Position " + number + ":");
        final FootballPlayer result = new FootballPlayer();
        result.leaderboardGoal = String.valueOf(number);
        result.name = players[number].split("name\\\":")[1].split(",\\\"")[0];
        result.team = players[number].split("name\\\":")[2].split(",\\\"")[0].split("}")[0];
        result.goals = players[number].split("numberOfGoals\\\":")[1].split(",\\\"")[0].split("}")[0];
        //System.out.println(result);
        return result;
    }

    public static Team fetchTeam(final String json, final int number) {
        final Pattern pattern = Pattern.compile("\\[.*\\]");
        Matcher matcher = pattern.matcher(json);
        String standings = "";
        while (matcher.find()) {
            standings = json.substring(matcher.start() + 1, matcher.end() - 1);
        }
        matcher = pattern.matcher(standings);
        String table = "";
        while (matcher.find()) {
            table = standings.substring(matcher.start(), matcher.end());
        }
//        System.out.println(table);
        final Team result = new Team();
        final String[] teams = table.split("position");
        result.position = String.valueOf(number);
        result.name = teams[number].split("name\\\":")[1].split(",\\\"")[0];
//        System.out.println(teams[number].split("name\\\":")[1].split(",\\\"")[0]);
        return result;
    }

    public static Question questionForPlayersTeam(final String json, final int notRandomIndex) {
        final FootballPlayer[] footballers = new FootballPlayer[services.QuestionsGenerator.NUMBER_ANSWERS];
        for (int i = 0; i < footballers.length; i++) {
            footballers[i] = services.QuestionsGenerator.fetchPlayer(json, i + 1);
        }

        final List<String> answers = new ArrayList<>();
        for (final FootballPlayer footballer : footballers
        ) {
            answers.add(footballer.team);
        }
        final StringBuilder content = new StringBuilder();

        final int correctAnswerIndex;
        if (notRandomIndex == -1) {
            correctAnswerIndex = new Random().nextInt(services.QuestionsGenerator.NUMBER_ANSWERS);
        }
        else {
            correctAnswerIndex = notRandomIndex;
        }

        content.append("\n").append("В кой отбор играе ").append(footballers[correctAnswerIndex].name)
                .append(", който има отбелязани ").append(footballers[correctAnswerIndex].goals)
                .append(" гола през този сезон?");
        return new Question(content.toString(), answers, correctAnswerIndex);
    }

    public static Question questionForPlayersGoalsNumber(final String json, final String league, final int notRandomIndex) {
        final int correctAnswerIndex;
        if (notRandomIndex == -1) {
            correctAnswerIndex = new Random().nextInt(services.QuestionsGenerator.NUMBER_ANSWERS);
        }
        else {
            correctAnswerIndex = notRandomIndex;
        }
        final FootballPlayer result = services.QuestionsGenerator.fetchPlayer(json, correctAnswerIndex + 1);
        final StringBuilder content = new StringBuilder();
        content.append("\n").append("Колко отбелязани гола има ")
                .append(result.name)
                .append(" в ").append(league)
                .append(" през този сезон?");
        final List<String> answers = new ArrayList<>();
        answers.add(result.goals);
        answers.add(String.valueOf(Integer.parseInt(result.goals) - 1));
        answers.add(String.valueOf(Integer.parseInt(result.goals) - 2));
        return new Question(content.toString(), answers, 0);
    }

    public static Question questionForLeagueScorers(final String json, final String league) {

        final FootballPlayer[] footballers = new FootballPlayer[services.QuestionsGenerator.NUMBER_ANSWERS];
        for (int i = 0; i < footballers.length; i++) {
            footballers[i] = services.QuestionsGenerator.fetchPlayer(json, i + 1);
        }

        final List<String> answers = new ArrayList<>();
        for (final FootballPlayer footballer : footballers
        ) {
            answers.add(footballer.name);
        }
        final StringBuilder content = new StringBuilder();
        content.append("\n").append("Кой e голмайсторът на ")
                .append(league)
                .append(" през този сезон в момента?");

        return new Question(content.toString(), answers, 0);
    }

    public static Question questionForLeagueLeaderboard(final String json, final String league, final int notRandomIndex) {
        final int correctAnswerIndex;
        if (notRandomIndex == -1) {
            correctAnswerIndex = new Random().nextInt(services.QuestionsGenerator.NUMBER_ANSWERS);
        }
        else {
            correctAnswerIndex = notRandomIndex;
        }
        final Team result = services.QuestionsGenerator.fetchTeam(json, correctAnswerIndex + 1);
        final StringBuilder content = new StringBuilder();
        content.append("\n")
                .append("На коя позиция в класирането се намира отборът ")
                .append(result.name)
                .append(" в ")
                .append(league)
                .append(" в момента?");
        final List<String> answers = new ArrayList<>();
        answers.add(result.position);
        answers.add(String.valueOf(Integer.parseInt(result.position) + 1));
        answers.add(String.valueOf(Integer.parseInt(result.position) + 2));
        return new Question(content.toString(), answers, 0);
    }

    static synchronized public List<Question> generate(final services.RequestSender retriever, final int notRandomIndex) throws Exception {
        final League leagueFirstQuestion;
        final League leagueSecondQuestion;
        if (notRandomIndex == -1) {
            leagueFirstQuestion = services.QuestionsGenerator.LEAGUE_CODES.get(new Random().nextInt(services.QuestionsGenerator.LEAGUE_CODES.size() - 1));
            leagueSecondQuestion = services.QuestionsGenerator.LEAGUE_CODES.get(new Random().nextInt(services.QuestionsGenerator.LEAGUE_CODES.size()));
        }
        else {
            leagueFirstQuestion = services.QuestionsGenerator.LEAGUE_CODES.get(notRandomIndex);
            leagueSecondQuestion = services.QuestionsGenerator.LEAGUE_CODES.get(notRandomIndex);
        }


        final List<Question> questions = new ArrayList<>();

        final CompletableFuture<String> future1 = services.RequestSender.getLeagueStanding(leagueFirstQuestion.code);

        future1.thenApply(s -> services.QuestionsGenerator.questionForLeagueLeaderboard(s, leagueFirstQuestion.name, -1))
                .thenAccept(questions::add);

        final CompletableFuture<String> future2 = services.RequestSender.getTopScorer(leagueSecondQuestion.code);

        future2.thenApply(resp -> {
            questions.add(QuestionsGenerator.questionForPlayersTeam(resp, -1));
            return resp;
        }).thenApply(resp -> {
            questions.add(QuestionsGenerator.questionForLeagueScorers(resp, leagueSecondQuestion.name));
            return resp;
        }).thenAccept(resp -> {
            questions.add(QuestionsGenerator.questionForPlayersGoalsNumber(resp, leagueSecondQuestion.name, -1));
        });


        future1.join();
        future2.join();
        System.out.println("----" + questions.size());

        return questions;
    }

    static public class FootballPlayer {
        String name;
        String team;
        String goals;
        String leaderboardGoal;

        FootballPlayer() {
        }

        public FootballPlayer(final String name, final String team, final String goals, final String leaderboardGoal) {
            this.name = name;
            this.team = team;
            this.goals = goals;
            this.leaderboardGoal = leaderboardGoal;
        }

        @Override
        public String toString() {
            return "FootballPlayer{" +
                    "name='" + name + '\'' +
                    ", team='" + team + '\'' +
                    ", goals='" + goals + '\'' +
                    ", leaderboardGoal='" + leaderboardGoal + '\'' +
                    '}';
        }
    }

    static public class Team {
        String name;
        String position;

        Team() {
        }

        public Team(final String name, final String position) {
            this.name = name;
            this.position = position;
        }

        @Override
        public String toString() {
            return "Team{" +
                    "name='" + name + '\'' +
                    ", position='" + position + '\'' +
                    '}';
        }
    }

    static public class League {
        private final String name;
        private final String code;

        League(final String name, final String code) {
            this.name = name;
            this.code = code;
        }
    }

}
