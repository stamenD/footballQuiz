package GameComponents.QuestionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestionsGenerator {

    static private class Player {
        String name;
        String team;
        String goals;
        String leaderboardGoal;

        Player() {
        }

        public Player(String name, String team, String goals, String leaderboardGoal) {
            this.name = name;
            this.team = team;
            this.goals = goals;
            this.leaderboardGoal = leaderboardGoal;
        }

        @Override
        public String toString() {
            return "Player{" +
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

    static private class League{
        private String name;
        private String code;

        public League(String name, String code) {
            this.name = name;
            this.code = code;
        }
    }

    private static final List<League> LEAGUE_CODES =
            List.of(
                    new League( "1. Bundesliga", "BL1"),
                    new League( "Premiere League", "PL"),
                    new League("Serie A", "SA"),
                    new League( "Primera Division", "PD"),
                    new League("Ligue 1", "FL1"),
                    new League("Champions-League", "CL")
            );


    private static Player fetchPlayer(String json, int number) {
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
        Player result = new Player();
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
        System.out.println("===========================================");
        System.out.println(json);
        System.out.println("===========================================");

        Player result1 = fetchPlayer(json, 1);
        Player result2 = fetchPlayer(json, 2);
        Player result3 = fetchPlayer(json, 3);

        List<String> answers = new ArrayList<>();
        answers.add(result1.team);
        answers.add(result2.team);
        answers.add(result3.team);
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("В кой отбор играе ").append(result1.name)
                .append(", който има отбелязани ").append(result1.goals)
                .append(" гола през този сезон?");
        return new Question(sb.toString(), answers, 0);
    }

    private static Question questionForPlayersGoalsNumber(String json, String league) {
        Player result = fetchPlayer(json, new Random().nextInt(3) + 1);
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("Колко отбелязани гола има ")
                .append(result.name)
                .append(" в ").append(league)
                .append(" през този сезон?");
        List<String> answers = new ArrayList<>();
        answers.add(result.goals);
        answers.add(String.valueOf(Integer.parseInt(result.goals) - 1));
        answers.add(String.valueOf(Integer.parseInt(result.goals) - 2));
        return new Question(sb.toString(), answers, 0);
    }

    private static Question questionForLeagueScorers(String json, String league) {
        Player result1 = fetchPlayer(json, 1);
        Player result2 = fetchPlayer(json, 2);
        Player result3 = fetchPlayer(json, 3);

        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("Кой e голмайсторът на ").append(league).
                append(" през този сезон в момента?");
        List<String> answers = new ArrayList<>();
        answers.add(result1.name);
        answers.add(result2.name);
        answers.add(result3.name);
        return new Question(sb.toString(), answers, 0);
    }

    private static Question questionForLeagueLeaderboard(String json, String league) {
        Team result = fetchTeam(json, new Random().nextInt(3) + 1);
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("На коя позиция в класирането се намира отборът ")
                .append(result.name)
                .append(" в ")
                .append(league)
                .append(" в момента?");
        List<String> answers = new ArrayList<>();
        answers.add(result.position);
        answers.add(String.valueOf(Integer.parseInt(result.position) + 1));
        answers.add(String.valueOf(Integer.parseInt(result.position) + 2));
        return new Question(sb.toString(), answers, 0);
    }

    synchronized public List<Question>  generate() throws Exception {
        RequestSender retriever = new RequestSender();
        League leagueFirstQuestion = LEAGUE_CODES.get(new Random().nextInt(LEAGUE_CODES.size()-1));
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
