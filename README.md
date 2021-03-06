# Stock-Trend-Predictor

## Project Summary
### Predicting Stock Trends Through Web Scraping the Wall Street Sub-Reddit

In this project, we sought to take available data from users' posts on the WallStreetBets sub-Reddit page, which currently has over 10 million active users. Our code web-scrapes the most recent daily discussion thread which typically contains over 10,000 comments from thousands of users on a given day. First, we scraped a website (https://www.stockanalysis.com/stocks/) for all stock tickers traded on the American exchanges and maintained a map between each ticker and their associated calculated “scores”. Then, through scraping and iterating through each comment, we determine the referenced stock and calculate its new score based on the contents of the comment. To process the contents of each post, we developed two text files of key words - one associated with a bullish outlook and the other with a bearish one. A stock’s score was incremented by 2 if a post had a bullish outlook, decreased by 2 if it had a bearish outlook, and incremented by 1 for each neutral mention. The program then finally outputs the top recommendations, which are stocks with the highest aggregate score.
	
	
 ### Categories implemented
 
For our Implementation Project, we primarily used Document Search through JSoup in order to retrieve the necessary data from the sub-Reddit r/wallstreetbets. To parse through all the comments on our discussion post of interest, the most recent Daily Discussion, we acquired the JSON file for the discussion page and collected the unique IDs associated with each comment thread. We then navigated through each comment thread parsing all the comments and processing its contents in order to score each stock. To process the comments’ contents, we used a list of keywords we developed and searched each comment for it to determine whether the outlook on a mentioned stock is bullish or bearish. Using all these tools, we were finally able to output recommendations using the highest scoring stocks based on individual user inputs.
	
### Task Distribution

In terms of the work breakdown structure, the complete group met and worked on planning and design of the program. This included the desired features and key methods including using JSoup. After the framework was decided on and the features we wanted to include were finalized, we split the work based on different aspects of the program. 
	
