# Sunami [Google Play](https://play.google.com/store/apps/details?id=com.wojtechnology.sunami&hl=en)

Sunami is the music player that learns your music taste as you listen to music. It can sense how you're feeling at the moment and picks music to match your vibes. Have a song missing from your library? Search SoundCloud for that song and add it to your library.
Features:
- Smart suggestion based on most played songs and genres
- SoundCloud integration
- Song queue
- Fast song search

### How it Works
The application takes into account different signals when ranking songs to play for you:
- Volatile/non-volatile song scores
  - If a user skips a song, score decreases and if they listen to the whole song, the score increases.
  - Use derivative of sigmoid curve to change scores (i.e. score changes less when it's close to 0 or 1 than when it is closer to 0.5).
- Most recent play
  - Recently played songs are less likely to be played.
- Genre score
  - Similar to song score but for a whole genre of music
  - Also looks at similar genres using genre similarity graph from Echo Nest API.
- Random
  - A random factor is added to the score.

The scores are recalculated after each song is played and the song with the highest score is added to the queue.

This project is licensed under the GNU General Public License.
