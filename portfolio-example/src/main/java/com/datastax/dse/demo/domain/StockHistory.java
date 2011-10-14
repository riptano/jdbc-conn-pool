package com.datastax.dse.demo.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StockHistory {

  private final String ticker;
  private Map<String,Double> history = new LinkedHashMap<String, Double>();
  
  
  public StockHistory(String ticker) {
    this.ticker = ticker;    
  }
  
  public String getTicker() {
    return ticker;
  }
  
  public void addDate(String dateString, Double price) {
    history.put(dateString, price);
  }
  
  public Map<String,Double> getHistory() {
    return history;
  }
  
  /**
   * Returns an unmodifiable view of the Prices
   * @return
   */
  public List<Double> getPriceHistory() {
    return Collections.unmodifiableList(new ArrayList<Double>(history.values()));
  }
}
