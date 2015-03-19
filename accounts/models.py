from django.db import models
from django.contrib.auth.models import User

# Create your models here.

# Model for user profiles
class UserProfile(models.Model):
	user = models.OneToOneField(User)

	status = models.CharField('User Status', max_length = 500, blank = True)
	display_picture = models.ImageField('User Display Picture', upload_to = 'display_pictures', blank = True)

	def __str__(self):
		return self.user.username