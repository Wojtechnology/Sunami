from django.db import models
from django.utils import timezone
from django.contrib.auth.models import User

# Create your models here.

# Model for user profiles
class UserProfile(models.Model):
	user = models.OneToOneField(User)

	confirmation_code = models.CharField('Confirmation Code', max_length = 50, default = '')
	is_password_reset = models.BooleanField('Password Reset Active', default = False)
	status = models.CharField('User Status', max_length = 500, blank = True)
	display_picture = models.ImageField('User Display Picture', upload_to = 'display_pictures', blank = True)

	def __str__(self):
		return self.user.username