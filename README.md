# Rack-Em
  This is a wordle based game created using JavaFX
## Description
This is a game created in JavaFX that is based of of wordle.
To Start:
  - Run the launcher
  - than you will be given 4 oprions
      - the top one is your theme. This is where you will choose what your backround is.
      - The nest one is your difficulties
          -We have baby, easy, medium, and hard mode
              - baby = 3 letters
              - easy = 4 letters
              - medium = 5 letters
              - hard = 6 letters
      - the third option is your icod which will be displayed in the top right corner
      - Lastly the fourth option is if you want to do a free play or the daily challenge
          - The daily challenge is one word that is randomly selected each day and doesn't change till the next day
          - the free play selects a new word every time you select it

## Project Creation
  - We used the JavaFX scene builder to create our multiple GUI's
## Project Structure
  - To begin you run the launcher which runs the "RackEmStartScreen" file
  - This file initializes the GUI and sets up all of the buttons, images, themes, anitmations, and other options
  - it also calls upon different classes in the "RackEmController" depending on the game mode
  - we use scanners to read the input and the way we check the answers is we
    1) take the input and the targetWord and splice them
    2) Compare the first letter of the input and the targetWord to see if the letter is in the right spot
        i) if yes turn letter green and move to step 4
        ii) if no move to step 3
    3) Compare the first letter to everery other letter in the targetWord
        i)if yes turn letter yellow
        ii) if no go to step 4
    4) Turn letter grey
   - However the way we have this set up creates an issue
      - Hear is an example
      - <img width="606" height="798" alt="Screenshot 2026-05-12 at 11 54 07 AM" src="https://github.com/user-attachments/assets/f229d7ba-36ea-48ce-b179-878fe17cf58f" />
      - in this example the word is apple
      - if you look at the third guess there is an error in the validation which is due to the way we set up our code
      - when the program gets to the first E in the third guess it turns it yellow because it realizes that there is an e in the targetWord "apple"
      - however by the rules of the game since the 3rd guess also has that same "E" in the correct spot the first "E" should be grey because there is only 1 "E"
      - this is a bug that we are aware of and are actively pursuing a fix
    
## Tutorials and Contributors
  - https://github.com/rgeorge-coach
