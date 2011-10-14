package com.datastax.dse.demo.domain;

/**
 * Models basic information about the stock - really just
 * ticker symbol and price
 * 
 * @author zznate
 */
public class Stock {
  
  private String ticker;
  private double price;
  
  public Stock() {
    
  }

  public Stock(String ticker, double price) {
    this.ticker = ticker;
    this.price = price;
  }

  public String getTicker() {
    return ticker;
  }

  public void setTicker(String ticker) {
    this.ticker = ticker;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }
  
  
}

