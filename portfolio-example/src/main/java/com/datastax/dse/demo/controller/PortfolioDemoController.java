package com.datastax.dse.demo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ui.Model;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.beans.factory.annotation.Autowired;

import com.datastax.dse.demo.domain.Portfolio;
import com.datastax.dse.demo.domain.StockHistory;

import com.datastax.dse.demo.dao.PortfolioDao;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PortfolioDemoController {

    public static final long MAX_DAYS            = 1000;
    public static final int  PORTFOLIOS_PER_PAGE = 1000;

    public static Logger logger = LoggerFactory.getLogger(PortfolioDemoController.class);

    private final PortfolioDao portfolioDao;

    @Autowired
    public PortfolioDemoController(PortfolioDao portfolioDao) {
        this.portfolioDao = portfolioDao;
    }

    @RequestMapping("/Portfolio")
    public ModelAndView getPortfolio(@RequestParam("id") String portfolioKey) {
      ModelAndView mav = new ModelAndView();

      Portfolio portfolio = portfolioDao.loadPortfolio(portfolioKey);

      logger.info("loaded portfolio for key : " + portfolioKey);

      mav.addObject("portfolio", portfolio);

      return mav;
    }

    @RequestMapping("/PortfolioList")
    public ModelAndView listPortfolios(@RequestParam(value = "startId", defaultValue="''") String startId,
                                       @RequestParam(value = "totalCount", defaultValue = "-1") long totalCount)
        throws Exception {

      ModelAndView mav = new ModelAndView();

      if (totalCount < 0) {
        // unitialized
        totalCount = portfolioDao.getPortfolioCount();
      }

      List<Portfolio> portList = null;
      if (totalCount > 0) {
        portList = portfolioDao.getPortfolios(startId, PORTFOLIOS_PER_PAGE);
        logger.info("loaded " + portList.size() + " portfolios.");
      } else {
        logger.info("No portfolios to load!");
      }

      mav.addObject("portfolioCount", totalCount);
      mav.addObject("portfolioList", portList);
      mav.addObject("pageSize", PORTFOLIOS_PER_PAGE);

      return mav;
    }
    
    @RequestMapping("/Stock")
    public Model getStock(@RequestParam("id") String stockKey, Model model) {

      StockHistory stockHist = portfolioDao.loadStock(MAX_DAYS, stockKey);

      logger.info("loaded price history for stock " + stockKey);

      model.addAttribute(stockHist);

      return model;
    }

    @RequestMapping("/Test")
    @ResponseBody
    public String test() {
      return "At the sound of the beep, the time will be [" + new java.util.Date().getTime() + "] *beep*";
    }
}
