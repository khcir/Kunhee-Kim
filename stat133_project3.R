# ===============================================
# Fill in the following fields
# ===============================================
# Title:
# Description:
# Author: 
# Date:


# ===============================================
# Packages
# ===============================================
library(tidyverse)
library(tidytext)
library(wordcloud)    # for plotting word clouds
library(RColorBrewer) # ColorBrewer palettes
library(igraph)       # for computing networks
library(ggraph)
library(tokenizers)
library(reshape2)
library(wordcloud2)


# ===============================================
# Import data
# ===============================================
# for demo purposes of the "template", we use data starwars
# (but you will have to replace this with the data in "u2-lyrics.csv")
#dat <- read.csv(file = "/Users/kundoongs/Desktop/STAT 133/project3/state-union-2001-2022.csv")
dat <- read.csv(file = "state-union-2001-2022.csv")
cleaned_dat <- dat %>% unnest_tokens(output = word,input = message) %>% anti_join(stop_words, by = "word")

bing_word_counts <- cleaned_dat %>%
  inner_join(get_sentiments("bing")) %>%
  count(word, sentiment, sort = TRUE) %>%
  ungroup()

# ===============================================
# Define UserInterface "ui" for application
# ===============================================
# Presidents = "George W. Bush"  "Barack Obama"    "Donald J. Trump" "Joseph R. Biden"

ui <- fluidPage(
  
  titlePanel("State of the Union Text Analysis"),
  fluidRow(
    # replace with your widgets
    column(3,
           p(em("Input widgets for Word Frequency Analysis")),
           radioButtons(inputId = "choose", 
                        label = "Choose one option", 
                        choices = c("Account All the Words" = "opt1",
                                    "Remove Stopwords" = "opt2"), 
                        selected = "opt1")
    ),
    
    # replace with your widgets
    column(3,
           p(em("Input widgets for Word Frequency Analysis")),
           selectInput(inputId = "numbers", 
                       label = "Most frequent words of",
                       choices = c("All Presidents" = "all",
                                   "George W. Bush" = "bush",
                                   "Barack Obama" = "obama",
                                   "Donald J. Trump" = "trump",
                                   "Joseph R. Biden" = "biden"),
                       selected = "all")
    ),
    
    column(3,
           p(em("Input widgets for Sentiment Analysis")),
           sliderInput(inputId = 'top5', 
                       label = 'Number of Top Words that Contributed to Sentiment', 
                       min = 1, 
                       max = 50,
                       value = 5,
                       step = 5)
    ),
    
    # replace with your widgets
    column(3,
           p(em("Input widgets for Sentiment Analysis")),
           selectInput(inputId = "sentanal", 
                        label = "Sentiment Analysis of:", 
                        choices = c("All Presidents" = "all",
                                    "George W. Bush" = "bush",
                                    "Barack Obama" = "obama",
                                    "Donald J. Trump" = "trump",
                                    "Joseph R. Biden" = "biden",
                                    "republican"= "republic",
                                    "democratic" = "democra"),
                        selected = "all")
    )
  ),
  hr(),
  
  tabsetPanel(type = "tabs",
              tabPanel("Analysis1",
                       h3("Word Frequency Analysis"),
                       plotOutput("barplot"),
                       hr(),
                       dataTableOutput('table1')),
              tabPanel("Analysis2", 
                       h3("Sentiment Analysis"),
                       plotOutput("cloud"),
                       hr(),
                       verbatimTextOutput('table2'))
  )
)


# ===============================================
# Define Server "server" logic
# ===============================================

server <- function(input, output) {
  
  # you may need to create reactive objects
  # (e.g. data frame to be used in barchart)
  dat_freq <- reactive({
    
    if (input$numbers == "all"){
      unnest_tokens(tbl = dat, output = word, input = message)['word'] %>% count(word) %>% arrange(desc(n))
    } else if(input$numbers == "bush"){
      unnest_tokens(tbl = filter(dat, president == "George W. Bush"), output = word, input = message)['word'] %>% count(word) %>% arrange(desc(n))
    } else if(input$numbers == "obama"){
      unnest_tokens(tbl = filter(dat, president == "Barack Obama"), output = word, input = message)['word'] %>% count(word) %>% arrange(desc(n))
    } else if (input$numbers == "trump"){
      unnest_tokens(tbl = filter(dat, president == "Donald J. Trump"), output = word, input = message)['word'] %>% count(word) %>% arrange(desc(n))
    } else if (input$numbers == "biden"){
      unnest_tokens(tbl = filter(dat, president == "Joseph R. Biden"), output = word, input = message)['word'] %>% count(word) %>% arrange(desc(n))
    }
    
    
    #filter(dat, president == "George W. Bush")
    #all_presidents <- unnest_tokens(tbl = dat, output = word, input = message)['word'] %>% count(word)
    
    #all_presidents_freqs %>% arrange(desc(n)) %>% slice_head(n = 10)
    #dat %>% group_by(sex) %>% count()
  })
  
  dat_freq_stopwords <- reactive({
    #unnest_tokens(tbl = dat, output = word, input = message)['word'] %>% anti_join(stop_words, by = "word") %>% count(word)count(word) %>% arrange(desc(n))
    if (input$numbers == "all"){
      unnest_tokens(tbl = dat, output = word, input = message)['word'] %>% anti_join(stop_words, by = "word") %>% count(word) %>% arrange(desc(n))
    } else if(input$numbers == "bush"){
      unnest_tokens(tbl = filter(dat, president == "George W. Bush"), output = word, input = message)['word'] %>% anti_join(stop_words, by = "word") %>% count(word) %>% arrange(desc(n))
    } else if(input$numbers == "obama"){
      unnest_tokens(tbl = filter(dat, president == "Barack Obama"), output = word, input = message)['word'] %>% anti_join(stop_words, by = "word") %>% count(word) %>% arrange(desc(n))
    } else if (input$numbers == "trump"){
      unnest_tokens(tbl = filter(dat, president == "Donald J. Trump"), output = word, input = message)['word'] %>% anti_join(stop_words, by = "word") %>% count(word) %>% arrange(desc(n))
    } else if (input$numbers == "biden"){
      unnest_tokens(tbl = filter(dat, president == "Joseph R. Biden"), output = word, input = message)['word'] %>% anti_join(stop_words, by = "word") %>% count(word) %>% arrange(desc(n))
    }
  })
  
  
  # ===============================================
  # Outputs for the first TAB (i.e. barchart)
  # ===============================================
  
  # code for barplot
  output$barplot <- renderPlot({
    # ggplot(data = head(dat_freq(),10),
    #        aes(x = reorder(word, -n), y = n)) +
    #   geom_col() + 
    #   labs(title = "Top 10 frequent words") +
    #   xlab("word") +
    #   ylab("count")
    
    if(input$choose == "opt1"){
      ggplot(data = head(dat_freq(),10),
             aes(x = reorder(word, -n), y = n)) +
        geom_col(fill = "#0073C2FF") + 
        labs(title = "Top 10 frequent words") +
        xlab("word") +
        ylab("count")
    }else{
      ggplot(data = head(dat_freq_stopwords(),10),
             aes(x = reorder(word, -n), y = n)) +
        geom_col(fill = "#0073C2FF") + 
        labs(title = "Top 10 frequent words") +
        xlab("word") +
        ylab("count")
    }
  })
  
  # code for numeric summaries of frequencies
  output$table1 <- renderDataTable({
    # replace the code below with your code!!!
    if(input$choose == "opt1"){
      dat_freq()
    }else{
      dat_freq_stopwords()
    }
  })
  
  ###################
  ####################
  cleaned_df <- reactive({
    
    if (input$sentanal == "all"){
      cleaned_dat
    } else if(input$sentanal == "bush"){
      cleaned_dat %>% filter(president == "George W. Bush")
    } else if(input$sentanal == "obama"){
      cleaned_dat %>% filter(president == "Barack Obama")
    } else if (input$sentanal == "trump"){
      cleaned_dat %>% filter(president == "Donald J. Trump")
    } else if (input$sentanal == "biden"){
      cleaned_dat %>% filter(president == "Joseph R. Biden")
    } else if (input$sentanal == "republic"){
      cleaned_dat %>% filter(party == "republican")
    } else if (input$sentanal == "democra"){
      cleaned_dat %>% filter(party == "democratic")
    }
  })
  sentiment_df <- reactive({
    cleaned_df() %>%
      inner_join(get_sentiments("bing")) %>%
      count(word, sentiment, sort = TRUE) %>%
      ungroup()
  })
  
  
  # ===============================================
  # Outputs for the second TAB (i.e. histogram)
  # ===============================================
  
  # code for histogram
  output$cloud <- renderPlot({
    #plotting barchart
    sentiment_df() %>%
      group_by(sentiment) %>%
      slice_max(n, n = input$top5) %>% 
      ungroup() %>%
      mutate(word = reorder(word, n)) %>%
      ggplot(aes(n, word, fill = sentiment)) +
      geom_col(show.legend = FALSE) +
      facet_wrap(~sentiment, scales = "free_y") +
      labs(x = "Contribution to sentiment",
           y = NULL)
    
  })
  
  # code for statistics
  output$table2 <- renderPrint({
    # replace the code below with your code!!!
    sentiment_df()
  })
  
}



# ===============================================
# Run the application
# ===============================================

shinyApp(ui = ui, server = server)

