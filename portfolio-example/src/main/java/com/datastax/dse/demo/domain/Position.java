package com.datastax.dse.demo.domain;

import java.util.Locale;
import java.text.NumberFormat;

import org.apache.commons.lang.StringUtils;

/**
 * Models the number of shares held for a stock 
 * 
 * @author zznate
 */
public class Position {
  private String ticker = StringUtils.EMPTY;
  private double price;
  private long shares;

  private static NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());

  public Position() {
    
  }
  
  public Position(String ticker, double price, long shares) {
    this.ticker = ticker;
    this.price = price;
    this.shares = shares;
  }
  
  public String getTicker() {
    return ticker;
  }
  
  public void setTicker(String ticker) {
    this.ticker = ticker;
  }

  public String getFormattedPrice() {
    return currencyFormat.format(price);
  }

  public double getPrice() {
    return price;
  }
  
  public void setPrice(double price) {
    this.price = price;
  }
  
  public long getShares() {
    return shares;
  }
  
  public void setShares(long shares) {
    this.shares = shares;
  }

  @Override
  public String toString() {
    return "Position [price=" + price + ", shares=" + shares + ", ticker="
        + ticker + "]";
  }

  /**
   * Compares based on the ticker since these are really only relevant within
   * the context of a Portfolio
   */
  @Override
  public boolean equals(Object obj) {
    if ( obj instanceof Position ) {
      return ((Position)obj).getTicker().equals(ticker);
    }
    return false;
  }

  /**
   * Calls hashCode on the ticker property
   */
  @Override
  public int hashCode() {
    return ticker.hashCode();
  }
  
  
  
  
}
