from django import forms
from django.contrib.auth.models import User
from accounts.models import UserProfile

class UserForm(forms.ModelForm):
	password = forms.CharField(widget = forms.PasswordInput())
	email = forms.CharField(widget = forms.EmailInput())

	def __init__(self, *args, **kwargs):
		super(UserForm, self).__init__(*args, **kwargs)

		self.fields['email'].required = True

	class Meta:
		model = User
		fields = ('username', 'email', 'password')

class UserProfileForm(forms.ModelForm):
	class Meta:
		model = UserProfile
		fields = ('status', 'display_picture')