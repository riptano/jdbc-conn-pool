package com.datastax.dse.demo.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Portfolio holds Position objects with some historical information
 *
 * @author zznate
 */
public class Portfolio {

  private String name;
  private Map<String,Position> constituents;
  private double basis;
  private double price;
  private double largest10dayLoss;
  private String largest10dayLossDate;
  private List<Double> histPrices;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Position> getConstituents() {
    return new ArrayList<Position>(constituents.values());
  }

  
  public double getBasis() {
    return basis;
  }

  public double getPrice() {
    return price;
  }

  public double getLargest10dayLoss() {
    return largest10dayLoss;
  }

  public void setLargest10dayLoss(double largest10dayLoss) {
    this.largest10dayLoss = largest10dayLoss;
  }

  public String getLargest10dayLossDate() {
    return largest10dayLossDate;
  }

  public void setLargest10dayLossDate(String largest10dayLossDate) {
    this.largest10dayLossDate = largest10dayLossDate;
  }

  public List<Double> getHistPrices() {
    return histPrices;
  }

  public void setHistPrices(List<Double> histPrices) {
    this.histPrices = histPrices;
  }
  
  public void addToConstituents(Position position) {
    if ( this.constituents == null ) {
      constituents = new HashMap<String,Position>();
    }
    constituents.put(position.getTicker(), position);
  }
  
  /**
   * Match up the {@link Stock} with the Position, updating {@link Position#getPrice()},
   * setting the price and basis of this portfolio along the way
   * @param stocks
   */
  public void applyStockPrices(List<Stock> stocks) {
    price = 0;
    basis = 0;
    Random random = new Random(Long.valueOf(name));
    for (Stock stock : stocks ) {
      Position p = constituents.get(stock.getTicker());
      if ( p != null ) {
        p.setPrice(stock.getPrice());
        price += stock.getPrice() * p.getShares();
        basis += p.getShares()  * 100 * random.nextDouble();
      }
    }
  }
  
}
