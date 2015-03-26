from django import forms, template
from django.contrib.auth.models import User
from accounts.models import UserProfile

class UserForm(forms.ModelForm):
	def __init__(self, *args, **kwargs):
		super(UserForm, self).__init__(*args, **kwargs)

		self.fields['email'].required = True

	class Meta:
		model = User
		fields = ('username', 'email', 'password')
		widgets = {
			'username' : forms.TextInput(attrs = {'placeholder' : 'Username'}),
			'password' : forms.PasswordInput(attrs = {'placeholder' : 'Password'}),
			'email' : forms.EmailInput(attrs = {'placeholder' : 'Email'})
			}

	# Enforces unique emails for accounts
	def clean_email(self):
		email = self.cleaned_data.get('email')
		username = self.cleaned_data.get('username')
		if email and User.objects.filter(email = email).exclude(username = username).count():
			raise forms.ValidationError('This Email is already in the system.')
		return email

class PasswordResetForm(forms.ModelForm):
	class Meta:
		model = User
		fields = ('password',)
		widgets = {'passwords' : forms.PasswordInput(attrs = {'placeholder' : 'Password'})}

	def clean_password(self):
		password = self.cleaned_data.get('password')
		if password_check(password):
			raise forms.ValidationError('Password eight characters or more.')
		return password

# Function that checks the password based on certain parameters
def password_check(password):
	return len(password) >= 8

class UserProfileForm(forms.ModelForm):
	class Meta:
		model = UserProfile
		fields = ('status', 'display_picture')