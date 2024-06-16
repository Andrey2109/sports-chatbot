package com.project.chatbot.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class HtmlProcessingService {

    public String processHtml(String html) {
        Document doc = Jsoup.parse(html);
        StringBuilder result = new StringBuilder();

        Elements competitionBlocks = doc.select("div.row > div.col-md-12.col-lg-12");
        String currentCompetitionType = null;

        for (Element block : competitionBlocks) {
            Elements typeElement = block.select(".box__title");
            if (!typeElement.isEmpty()) {
                currentCompetitionType = typeElement.text().trim();
                result.append("Competition Type: ").append(currentCompetitionType).append("\n");
            }

            Elements nameElements = block.select(".expander__title");
            if (!nameElements.isEmpty() && currentCompetitionType != null) {
                for (Element nameElement : nameElements) {
                    String competitionName = nameElement.text().trim();
                    result.append("Competition Name: ").append(competitionName).append("\n");

                    Elements panels = block.select(".panel");
                    for (Element panel : panels) {
                        Element apparatusElement = panel.selectFirst(".panel-heading strong");
                        if (apparatusElement != null) {
                            String apparatus = apparatusElement.text().trim();
                            result.append("Apparatus: ").append(apparatus).append("\n");

                            Elements rows = panel.select(".row");
                            for (Element row : rows) {
                                Elements cols = row.select(".col-xs-6.text-left");
                                if (cols.size() == 2) {
                                    String rankOrScoreType = cols.get(0).text().trim();
                                    String rankOrScoreValue = cols.get(1).text().trim();
                                    result.append(rankOrScoreType).append(": ").append(rankOrScoreValue).append("\n");
                                }
                            }
                        }
                    }
                }
            }
        }
        return result.toString();
    }
}