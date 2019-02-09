import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import GameComponents.QuestionUtils.Question;
import GameComponents.QuestionUtils.QuestionsGenerator;
import GameComponents.QuestionUtils.RequestSender;
import org.junit.Test;
import org.mockito.Mock;

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
    public void testCanCreateQuestion()  {
        StringBuilder content = new StringBuilder();
        content.append("\n")
                .append("На коя позиция в класирането се намира отборът ")
                .append("\"Liverpool FC\"")
                .append(" в ")
                .append("Premiere League")
                .append(" в момента?");

        List<String> answers = Arrays.asList("1","2","3");
        Question expect = new Question(content.toString(), answers, 0);
        String result = "\n\nНа коя позиция в класирането се намира отборът \"Liverpool FC\" в Premiere League в момента?\n" +
                "0)1\n" +
                "1)2\n" +
                "2)3";
        assertEquals(expect.toString(),result.toString());
    }

    @Test
    public void testToCreateQuestionForLeagueLeaderboard()  {
        StringBuilder content = new StringBuilder();
        content.append("\n")
                .append("На коя позиция в класирането се намира отборът ")
                .append("\"Liverpool FC\"")
                .append(" в ")
                .append("Premiere League")
                .append(" в момента?");

        List<String> answers = Arrays.asList("1","2","3");
        Question expect = new Question(content.toString(), answers, 0);

        Question result = QuestionsGenerator.questionForLeagueLeaderboard(TEST_RESPONSE2,"Premiere League",0);

        assertEquals(expect.toString(),result.toString());
    }

    @Test
    public void testToCreateQuestionForLeagueScorers()  {
        StringBuilder content = new StringBuilder();
        content.append("\n").append("Кой e голмайсторът на ")
                .append("Premiere League")
                .append(" през този сезон в момента?");

        List<String> answers = Arrays.asList("\"Mohamed Salah\"","\"Pierre-Emerick Aubameyang\"","\"Harry Kane\"");
        Question expect = new Question(content.toString(), answers, 0);

        Question result = QuestionsGenerator.questionForLeagueScorers(TEST_RESPONSE,"Premiere League");

        assertEquals(expect.toString(),result.toString());
    }

    @Test
    public void testToCreateQuestionForPlayersGoalsNumber()  {
        StringBuilder content = new StringBuilder();
        content.append("\n").append("Колко отбелязани гола има ")
                .append("\"Mohamed Salah\"")
                .append(" в ").append("Premiere League")
                .append(" през този сезон?");

        List<String> answers = Arrays.asList("16","15","14");
        Question expect = new Question(content.toString(), answers, 0);

        Question result = QuestionsGenerator.questionForPlayersGoalsNumber(TEST_RESPONSE,"Premiere League",0);

        assertEquals(expect.toString(),result.toString());
    }

    @Test
    public void testToCreateQuestionForPlayersTeam()  {
        StringBuilder content = new StringBuilder();
        content.append("\n").append("В кой отбор играе ").append("\"Mohamed Salah\"")
                .append(", който има отбелязани ").append("16")
                .append(" гола през този сезон?");

        List<String> answers = Arrays.asList("\"Liverpool FC\"","\"Arsenal FC\"","\"Tottenham Hotspur FC\"");
        Question expect = new Question(content.toString(), answers, 0);

        Question result = QuestionsGenerator.questionForPlayersTeam(TEST_RESPONSE,0);

        assertEquals(expect.toString(),result.toString());
    }

    @Test
    public void testToFetchPlayer(){
        QuestionsGenerator.FootballPlayer expect =
                new QuestionsGenerator.FootballPlayer("\"Mohamed Salah\"","\"Liverpool FC\"","16","1");
        QuestionsGenerator.FootballPlayer result = QuestionsGenerator.fetchPlayer("["+TEST_RESPONSE+"]",1);
        assertEquals(expect.toString(),result.toString());
    }

    @Test
    public void testToFetchFirstTeam(){
        QuestionsGenerator.Team expect =
                new QuestionsGenerator.Team("\"Liverpool FC\"","1");
        QuestionsGenerator.Team result = QuestionsGenerator.fetchTeam("["+TEST_RESPONSE2+"]",1);
        assertEquals(expect.toString(),result.toString());
    }

    @Test
    public void testToFetchLastTeam(){
        QuestionsGenerator.Team expect =
                new QuestionsGenerator.Team("\"Tottenham Hotspur FC\"","3");
        QuestionsGenerator.Team result = QuestionsGenerator.fetchTeam("["+TEST_RESPONSE2+"]",3);
        assertEquals(expect.toString(),result.toString());
    }

    @Test
    public void testGenerateQuestionsSize() throws Exception {
        RequestSender requestSenderMock = mock(RequestSender.class);
        CompletableFuture<String> responseMockOne = CompletableFuture.completedFuture("["+TEST_RESPONSE2+"]");
        CompletableFuture<String> responseMockTwo = CompletableFuture.completedFuture("["+TEST_RESPONSE+"]");

        when(requestSenderMock.getLeagueStanding("SA"))
                .thenReturn(responseMockOne);

        when(requestSenderMock.getTopScorer("SA"))
                .thenReturn(responseMockTwo);

        QuestionsGenerator test = new QuestionsGenerator();
        assertEquals(4,   test.generate(requestSenderMock,2).size());
    }

}
