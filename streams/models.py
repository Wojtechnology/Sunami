from django.db import models
from django.utils import timezone
from accounts.models import UserProfile

# Model for past streams belonging to a user
class Stream(models.Model):
	owner = models.ForeignKey(UserProfile)

	# Tells whether the current stream is playing or just an archive
	is_active = models.BooleanField('Stream is Active', default = False)

	# General information about stream
	stream_title = models.CharField('Title of Stream', max_length = 200)
	genre = models.CharField('Genre of Stream', max_length = 50)
	num_songs = models.IntegerField('Number of Songs', default = 0)
	cur_num_listeners = models.IntegerField('Current Number of Listeners', default = 0)
	max_num_listeners = models.IntegerField('Maximum Number of Listeners', default = 0)
	favourites = models.IntegerField('Number of Likes', default = 0)
	comments = models.IntegerField('Number of Comments', default = 0)
	start_date_time = models.DateTimeField('Stream Start Time', default = timezone.now())
	end_date_time = models.DateTimeField('Stream End Time', default = timezone.now())

# Comment model for streams
class CommentStream(models.Model):
	stream = models.ForeignKey(Stream)
	owner = models.ForeignKey(UserProfile)

	# General information about comment
	pub_date = models.DateTimeField('Publish Date', default = timezone.now())
	text = models.CharField('Comment Text', max_length = 500)
	likes = models.IntegerField('Comment Likes', default = 0)

# Add model for songs
# Refers to an instance of a Sound Cloud song play
class SoundCloudSong(models.Model):
	stream = models.ForeignKey(Stream)

	# General song information
	# Song name
	# Artist
	# Album (Optional)
	# Look for more on SoundCloud API