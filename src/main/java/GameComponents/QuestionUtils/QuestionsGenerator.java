package GameComponents.QuestionUtils;

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

    static private class FootballPlayer {
        String name;
        String team;
        String goals;
        String leaderboardGoal;

        FootballPlayer() {
        }

        public FootballPlayer(String name, String team, String goals, String leaderboardGoal) {
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

    static private class Team {
        String name;
        String position;
    }

    static private class League {
        private String name;
        private String code;

        public League(String name, String code) {
            this.name = name;
            this.code = code;
        }
    }




    private static FootballPlayer fetchPlayer(String json, int number) {
//        System.out.println(json);
        Pattern pattern = Pattern.compile("\\[.*\\]");
        Matcher matcher = pattern.matcher(json);
        String scorers = "";
        while (matcher.find()) {
            scorers = json.substring(matcher.start() + 1, matcher.end() - 1);
        }
//        System.out.println(scorers);

        String[] players = scorers.split("player");
//        System.out.println("Position " + number + ":");
        FootballPlayer result = new FootballPlayer();
        result.leaderboardGoal = String.valueOf(number);
        result.name = players[number].split("name\\\":")[1].split(",\\\"")[0];
        result.team = players[number].split("name\\\":")[2].split(",\\\"")[0].split("}")[0];
        result.goals = players[number].split("numberOfGoals\\\":")[1].split(",\\\"")[0].split("}")[0];
        return result;
    }

    private static Team fetchTeam(String json, int number) {
        Pattern pattern = Pattern.compile("\\[.*\\]");
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
        Team result = new Team();
        String[] teams = table.split("position");
        result.position = String.valueOf(number);
        result.name = teams[number].split("name\\\":")[1].split(",\\\"")[0];
//        System.out.println(teams[number].split("name\\\":")[1].split(",\\\"")[0]);
        return result;
    }

    private static Question questionForPlayersTeam(String json) {
        FootballPlayer[] footballers = new FootballPlayer[NUMBER_ANSWERS];
        for (int i = 0; i < footballers.length; i++) {
            footballers[i] = fetchPlayer(json, i);
        }

        List<String> answers = new ArrayList<>();
        for (FootballPlayer footballer : footballers
        ) {
            answers.add(footballer.team);
        }
        StringBuilder content = new StringBuilder();

        int correctAnswerIndex = new Random().nextInt(NUMBER_ANSWERS);
        content.append("\n").append("В кой отбор играе ").append(footballers[correctAnswerIndex].name)
                .append(", който има отбелязани ").append(footballers[correctAnswerIndex].goals)
                .append(" гола през този сезон?");
        return new Question(content.toString(), answers, correctAnswerIndex);
    }

    private static Question questionForPlayersGoalsNumber(String json, String league) {
        FootballPlayer result = fetchPlayer(json, new Random().nextInt(NUMBER_ANSWERS) + 1);
        StringBuilder content = new StringBuilder();
        content.append("\n").append("Колко отбелязани гола има ")
                .append(result.name)
                .append(" в ").append(league)
                .append(" през този сезон?");
        List<String> answers = new ArrayList<>();
        answers.add(result.goals);
        answers.add(String.valueOf(Integer.parseInt(result.goals) - 1));
        answers.add(String.valueOf(Integer.parseInt(result.goals) - 2));
        return new Question(content.toString(), answers, 0);
    }

    private static Question questionForLeagueScorers(String json, String league) {

        FootballPlayer[] footballers = new FootballPlayer[NUMBER_ANSWERS];
        for (int i = 0; i < footballers.length; i++) {
            footballers[i] = fetchPlayer(json, i);
        }

        List<String> answers = new ArrayList<>();
        for (FootballPlayer footballer : footballers
        ) {
            answers.add(footballer.name);
        }
        StringBuilder content = new StringBuilder();
        content.append("\n").append("Кой e голмайсторът на ")
                .append(league)
                .append(" през този сезон в момента?");

        return new Question(content.toString(), answers, 0);
    }

    private static Question questionForLeagueLeaderboard(String json, String league) {
        Team result = fetchTeam(json, new Random().nextInt(NUMBER_ANSWERS) + 1);
        StringBuilder content = new StringBuilder();
        content.append("\n")
                .append("На коя позиция в класирането се намира отборът ")
                .append(result.name)
                .append(" в ")
                .append(league)
                .append(" в момента?");
        List<String> answers = new ArrayList<>();
        answers.add(result.position);
        answers.add(String.valueOf(Integer.parseInt(result.position) + 1));
        answers.add(String.valueOf(Integer.parseInt(result.position) + 2));
        return new Question(content.toString(), answers, 0);
    }

    synchronized public List<Question> generate() throws Exception {
        RequestSender retriever = new RequestSender();
        League leagueFirstQuestion = LEAGUE_CODES.get(new Random().nextInt(LEAGUE_CODES.size() - 1));
        League leagueSecondQuestion = LEAGUE_CODES.get(new Random().nextInt(LEAGUE_CODES.size()));

        List<Question> questions = new ArrayList<>();

        CompletableFuture<String> future1 = retriever.getLeagueStanding(leagueFirstQuestion.code);

        future1.thenApply(s -> questionForLeagueLeaderboard(s, leagueFirstQuestion.name))
                .thenAccept(questions::add);

        CompletableFuture<String> future2 = retriever.getTopScorer(leagueSecondQuestion.code);

        future2.thenApply(resp -> {
            questions.add(QuestionsGenerator.questionForPlayersTeam(resp));
            return resp;
        }).thenApply(resp -> {
            questions.add(QuestionsGenerator.questionForLeagueScorers(resp, leagueSecondQuestion.name));
            return resp;
        }).thenAccept(resp -> {
            questions.add(QuestionsGenerator.questionForPlayersGoalsNumber(resp, leagueSecondQuestion.name));
        });


        future1.join();
        future2.join();


        return questions;
    }

}
