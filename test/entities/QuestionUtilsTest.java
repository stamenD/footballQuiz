package entities;

import org.junit.Test;
import org.mockito.Mock;
import services.QuestionsGenerator;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class QuestionUtilsTest {
    private final static String TEST_RESPONSE = "{\"count\":3,\"filters\":{\"limit\":3}," +
            "\"competition\":{\"id\":2021,\"area\":{\"id\":2072,\"name\":\"England\"}" +
            ",\"name\":\"Premier League\",\"code\":\"PL\",\"plan\":\"TIER_ONE\",\"lastUpdated\"" +
            ":\"2019-01-30T20:09:42Z\"},\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"end" +
            "Date\":\"2019-05-12\",\"currentMatchday\":24,\"winner\":null},\"scorers\":[{\"player\":{" +
            "\"id\":3754,\"name\":\"Mohamed Salah\",\"firstName\":\"Mohamed Salah\",\"lastName\":null,\"" +
            "dateOfBirth\":\"1992-06-15\",\"countryOfBirth\":\"Egypt\",\"nationality\":\"Egypt\",\"position\"" +
            ":\"Attacker\",\"shirtNumber\":11,\"lastUpdated\":\"2018-11-25T05:07:31Z\"},\"team\":{\"id\":64,\"name\"" +
            ":\"Liverpool FC\"},\"numberOfGoals\":16},{\"player\":{\"id\":7801,\"name\":\"Pierre-Emerick Aubameyang\"," +
            "\"firstName\":\"Pierre-Emerick Emiliano François\",\"lastName\":null,\"dateOfBirth\":\"1989-06-18\"," +
            "\"countryOfBirth\":\"France\",\"nationality\":\"Gabon\",\"position\":\"Attacker\",\"shirtNumber\":14" +
            ",\"lastUpdated\":\"2018-09-19T05:12:36Z\"},\"team\":{\"id\":57,\"name\":\"Arsenal FC\"},\"numberOfGoals\":15}" +
            ",{\"player\":{\"id\":8004,\"name\":\"Harry Kane\",\"firstName\":\"Harry\",\"lastName\":null,\"dateOfBirth\":" +
            "\"1993-07-28\",\"countryOfBirth\":\"England\",\"nationality\":\"England\",\"position\":\"Attacker\",\"shirtNumber\"" +
            ":10,\"lastUpdated\":\"2018-11-25T05:14:21Z\"},\"team\":{\"id\":73,\"name\":\"Tottenham Hotspur FC\"},\"numberOfGoal" +
            "s\":14}]}";


    private final static String TEST_RESPONSE2 = "{\"filters\":{},\"competition\":{\"id\":2021,\"area\":{\"id\":2072,\"name\":" +
            "\"England\"},\"name\":\"Premier League\",\"code\":\"PL\",\"plan\":\"TIER_ONE\",\"lastUpdated\":\"2019-01-30T20:18:42Z\"}," +
            "\"season\":{\"id\":151,\"startDate\":\"2018-08-10\",\"endDate\":\"2019-05-12\",\"currentMatchday\":24,\"winner\":null}," +
            "\"standings\":[{\"stage\":\"REGULAR_SEASON\",\"type\":\"TOTAL\",\"group\":null,\"table\":[{\"position\":1,\"team\":" +
            "{\"id\":64,\"name\":\"Liverpool FC\",\"crestUrl\":\"http://upload.wikimedia.org/wikipedia/de/0/0a/FC_Liverpool.svg\"}" +
            ",\"playedGames\":23,\"won\":19,\"draw\":3,\"lost\":1,\"points\":60,\"goalsFor\":54,\"goalsAgainst\":13," +
            "\"goalDifference\":41},{\"position\":2,\"team\":{\"id\":65,\"name\":\"Manchester City FC\",\"crestUrl\"" +
            ":\"https://upload.wikimedia.org/wikipedia/en/e/eb/Manchester_City_FC_badge.svg\"},\"playedGames\":24,\"won\"" +
            ":18,\"draw\":2,\"lost\":4,\"points\":56,\"goalsFor\":63,\"goalsAgainst\":19,\"goalDifference\":44},{\"position\"" +
            ":3,\"team\":{\"id\":73,\"name\":\"Tottenham Hotspur FC\",\"crestUrl\":" +
            "\"http://upload.wikimedia.org/wikipedia/de/b/b4/Tottenham_Hotspur.svg\"},\"playedGames\":23,\"won\":17," +
            "\"draw\":0,\"lost\":6,\"points\":51,\"goalsFor\":48,\"goalsAgainst\":23,\"goalDifference\":25},{\"position\":20,\"team\":{\"id\":394,\"name\":" +
            "\"Huddersfield Town AFC\",\"crestUrl\":\"https://upload.wikimedia.org/wikipedia/en/5/5a/Huddersfield_Town_A.F.C._logo.svg\"}," +
            "\"playedGames\":24,\"won\":2,\"draw\":5,\"lost\":17,\"points\":11,\"goalsFor\"" +
            ":13,\"goalsAgainst\":41,\"goalDifference\":-28}]}]}";

    @Mock
    private HttpClient httpClientMock;

    @Mock
    private HttpResponse<String> httpResponseMock;


    @Test
    public void testCanCreateQuestion() {

        final List<String> answers = Arrays.asList("1", "2", "3");
        final String content = "\n" +
                "На коя позиция в класирането се намира отборът " +
                "\"Liverpool FC\"" +
                " в " +
                "Premiere League" +
                " в момента?";
        final Question expect = new Question(content, answers, 0);
        final String result = "\n\nНа коя позиция в класирането се намира отборът \"Liverpool FC\" в Premiere League в момента?\n" +
                "0)1\n" +
                "1)2\n" +
                "2)3";
        assertEquals(expect.toString(), result);
    }

    @Test
    public void testToCreateQuestionForLeagueLeaderboard() {

        final List<String> answers = Arrays.asList("1", "2", "3");
        final String content =
                "\nIn which position is \"Liverpool FC\" in Premiere League at the moment?";
        final Question expect = new Question(content, answers, 0);

        final Question result = QuestionsGenerator.questionForLeagueLeaderboard(QuestionUtilsTest.TEST_RESPONSE2, "Premiere League", 0);

        assertEquals(expect.toString(), result.toString());
    }

    @Test
    public void testToCreateQuestionForLeagueScorers() {

        final List<String> answers = Arrays.asList("\"Mohamed Salah\"", "\"Pierre-Emerick Aubameyang\"", "\"Harry Kane\"");

        final String content = "\n" + "Who is the top scorer in Premiere League at the moment?";

        final Question expect = new Question(content, answers, 0);

        final Question result = QuestionsGenerator.questionForLeagueScorers(QuestionUtilsTest.TEST_RESPONSE, "Premiere League");

        assertEquals(expect.toString(), result.toString());
    }

    @Test
    public void testToCreateQuestionForPlayersGoalsNumber() {

        final List<String> answers = Arrays.asList("16", "15", "14");
        final String content = "\nHow many goals did \"Mohamed Salah\" score in Premiere League during this season?";
        final Question expect = new Question(content, answers, 0);

        final Question result = QuestionsGenerator.questionForPlayersGoalsNumber(QuestionUtilsTest.TEST_RESPONSE, "Premiere League", 0);

        assertEquals(expect.toString(), result.toString());
    }

    @Test
    public void testToCreateQuestionForPlayersTeam() {

        final List<String> answers = Arrays.asList("\"Liverpool FC\"", "\"Arsenal FC\"", "\"Tottenham Hotspur FC\"");
        final String content = "\nFor which team does \"Mohamed Salah\" play(he has scored 16 goals this season)?";
        final Question expect = new Question(content, answers, 0);

        final Question result = QuestionsGenerator.questionForPlayersTeam(QuestionUtilsTest.TEST_RESPONSE, 0);

        assertEquals(expect.toString(), result.toString());
    }

    @Test
    public void testToFetchPlayer() {
        final QuestionsGenerator.FootballPlayer expect =
                new QuestionsGenerator.FootballPlayer("\"Mohamed Salah\"", "\"Liverpool FC\"", "16", "1");
        final QuestionsGenerator.FootballPlayer result = QuestionsGenerator.fetchPlayer("[" + QuestionUtilsTest.TEST_RESPONSE + "]", 1);
        assertEquals(expect.toString(), result.toString());
    }

    @Test
    public void testToFetchFirstTeam() {
        final QuestionsGenerator.Team expect =
                new QuestionsGenerator.Team("\"Liverpool FC\"", "1");
        final QuestionsGenerator.Team result = QuestionsGenerator.fetchTeam("[" + QuestionUtilsTest.TEST_RESPONSE2 + "]", 1);
        assertEquals(expect.toString(), result.toString());
    }

    @Test
    public void testToFetchLastTeam() {
        final QuestionsGenerator.Team expect =
                new QuestionsGenerator.Team("\"Tottenham Hotspur FC\"", "3");
        final QuestionsGenerator.Team result = QuestionsGenerator.fetchTeam("[" + QuestionUtilsTest.TEST_RESPONSE2 + "]", 3);
        assertEquals(expect.toString(), result.toString());
    }

//    @Test
//    public void testGenerateQuestionsSize() throws Exception {
//        final RequestSender requestSenderMock = mock(RequestSender.class);
//        final CompletableFuture<String> responseMockOne = CompletableFuture.completedFuture("[" + QuestionUtilsTest.TEST_RESPONSE2 + "]");
//        final CompletableFuture<String> responseMockTwo = CompletableFuture.completedFuture("[" + QuestionUtilsTest.TEST_RESPONSE + "]");
//
//        when(services.RequestSender.getLeagueStanding("SA"))
//                .thenReturn(responseMockOne);
//
//        when(requestSenderMock.getTopScorer("SA"))
//                .thenReturn(responseMockTwo);
//
//        final QuestionsGenerator test = new QuestionsGenerator();
//        assertEquals(4, test.generate(requestSenderMock, 2).size());
//    }

}
