# DiceWeightCalculator

This is a small app I wrote for wife so she could calculate the balanced of her dice for Dungeons and Dragons

Core functionality
- Landiing page shows global stats for mean, median, mode, odd, and even rolls for all dice types
- Sessions Screen shows each session in a list
  - tapping a session will expand the card and show more data
  - long pressing the card will allow for deleting that session
- Fab starts a new session
  - you can set the session name
  - you can set how many rolls per dice
  - you can select which dice you want to roll(default is D20 only)
- Current Session
  - will ask for input for each roll for each dice celected
  - uses number pad
  - validation is in place to make sure the number entered is within the range of the dice faces
    - e.g. you can't enter a 7 for a D6 roll, you can't enter a 57 for a percent dice roll
  - after all rolls are done, next screen shows results

I use a few AlertDialogs for
  - delete session
  - if you exit current session before you have recorded all dice rolls

# //TODO:
- I need to fix a small issue with Text fields that allow for Enter from number pad to make a new line
- Settings screen for
  - select dark mode, light mode, or system default
  - import/export
  - add support for CashApp in Donation
- testing, breaking, fixing
- branding, logo, name

