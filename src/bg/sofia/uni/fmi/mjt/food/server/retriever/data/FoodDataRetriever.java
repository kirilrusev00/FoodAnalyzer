package bg.sofia.uni.fmi.mjt.food.server.retriever.data;

import bg.sofia.uni.fmi.mjt.food.server.retriever.FoodInfoRetrieverImpl;
import bg.sofia.uni.fmi.mjt.food.server.retriever.data.search.Food;
import bg.sofia.uni.fmi.mjt.food.server.retriever.data.search.FoodSearch;
import com.google.gson.Gson;

import java.net.http.HttpClient;

public class FoodDataRetriever extends FoodInfoRetrieverImpl {
    private static final String GENERAL_SEARCH_TEMPLATE = "&generalSearchInput=";
    private static final String KEY_WORD_FOR_SEARCH = "search";

    public FoodDataRetriever(HttpClient client, String apiKey) {
        super(client, apiKey);
    }

    @Override
    public String buildAnalyzerURIString(String searchInput) {
        return String.format(getAnalyzerUriTemplate(), KEY_WORD_FOR_SEARCH, getApiKey())
                + GENERAL_SEARCH_TEMPLATE + searchInput.replace(" ", "%20");
    }

    @Override
    public String getRequiredInformationAsString(String searchInput) {
        String cacheInfo = checkInCache(searchInput);
        if (cacheInfo != null) {
            System.out.println("Found in cache!");
            return cacheInfo;
        }

        FoodSearch foodSearch = getRequiredInformation(searchInput);
        if (!foodSearch.getFoods().isEmpty()) {
            addToCache(searchInput, foodSearch.getFoods().toString());
            for (Food food : foodSearch.getFoods()) {
                addToGtinUpcCache(food.getGtinUpc(), food.toString());
            }
            return foodSearch.getFoods().toString();
        }
        final String notFoundMessage = "No food matching the given input";
        addToCache(searchInput, notFoundMessage);
        return notFoundMessage;
    }

    public FoodSearch getRequiredInformation(String searchInput) {
        System.out.println("Sending request to FoodData Central!");
        String analyzerJsonResponse = retrieveInformationFromFDC(searchInput);

        Gson gson = new Gson();
        return gson.fromJson(analyzerJsonResponse, FoodSearch.class);
    }
}
