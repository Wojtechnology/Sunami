from django.shortcuts import render, redirect
from django.contrib.auth import authenticate, login, logout
from django.views.generic import View
from django.utils import timezone
from django.contrib.auth.models import User
from django.contrib.auth.decorators import login_required
from accounts.models import UserProfile
from accounts.forms import UserForm, PasswordResetForm
from django.core.mail import send_mail

import random
import string

# Login required wrapper for as_view method for class views
# May be needed later
#class LoginRequiredMixin(object):
#	@classmethod
#	def as_view(cls, **initkwargs):
#		view = super(LoginRequiredMixin, cls).as_view(**initkwargs)
#		return login_required(view)

# Class to login to user profile
# Write tests for this
class LoginView(View):
	def get(self, request):
		# If user already logged in, redirect to home page
		if request.user.is_authenticated():
			return redirect('index')

		return render(request, 'accounts/login.html', {'page_title' : 'Sunami - Login'})

	def post(self, request):
		errors = []

		email = request.POST.get('email')
		password = request.POST.get('password')

		# Find username based on email address given
		username = ''

		try:
			username = User.objects.get(email = email).username
		except:
			errors.append('This Email does not exist')

		user = authenticate(username = username, password = password)

		# If user could be found, login user
		if user:

			# If user is active, send back to homepage logged in
			if user.is_active:
				login(request, user)
				return redirect('index')

			else:
				errors.append('Account is inactive')

		else:
			errors.append('Password does not match Email')

		return render(request, 'accounts/login.html', {'errors' : errors[0:1], 'page_title' : 'Sunami - Login'})


# Class to logout and redirect 
class LogoutView(View):
	def get(self, request):
		logout(request)
		return redirect('index')

# Generate confirmation code
def generate_code():
	confirmation_code = ''.join(random.SystemRandom().choice(string.ascii_uppercase + string.digits) for i in range(33))
	return confirmation_code

# Send Registration Email
def send_registration_confirmation(user):
	profile = user.userprofile
	title = 'Sunami Account Confirmation'
	content = 'http://127.0.0.1:8000/profile/confirm/' + profile.confirmation_code + '/' + user.username
	send_mail(title, content, 'sunamisound@gmail.com', [user.email])

# Send Password Reset Email
def send_password_reset(user):
	profile = user.userprofile
	title = 'Sunami Password Reset'
	content = 'http://127.0.0.1:8000/profile/passreset/' + profile.confirmation_code + '/' + user.username
	send_mail(title, content, 'sunamisound@gmail.com', [user.email])

# Class to authenticate user
class ConfirmView(View):
	def get(self, request, confirmation_code, username):
		try:
			user = User.objects.get(username = username)
			profile = user.userprofile

			# Check if confirmation code is correct
			if profile.confirmation_code == confirmation_code:
				user.is_active = True
				user.save()
				return render(request, 'accounts/confirm.html', {'confirmed' : True, 
					'page_title' : 'Sunami - Account Confirmation'})

		except:
			pass

		# This is what shows up if the confirmation code is wrong
		return render(request, 'accounts/confirm.html', {'confirmed' : False, 
			'page_title' : 'Sunami - Accounts Confirmation'})

# Class to reset password
class IForgotView(View):
	def get(self, request):
		return render(request, 'accounts/iforgot.html', {'page_title' : 'Sunami - Password Reset'})

	def post(self, request):
		errors = []

		email = request.POST.get('email')

		# Find user
		user = None

		try:
			user = User.objects.get(email = email)
		except:
			errors.append('This Email does not exist')

		if user:
			profile = user.userprofile
			profile.confirmation_code = generate_code()
			profile.is_password_reset = True
			profile.save()

			if user.is_active:
				send_password_reset(user)
				return render(request, 'accounts/iforgot.html', {'errors' : errors[0:1], 'page_title' : 'Sunami - Password Reset', 'sent': True})

			else:
				errors.append('Account is inactive')

		return render(request, 'accounts/iforgot.html', {'errors' : errors[0:1], 'page_title' : 'Sunami - Password Reset'})

# Class to receive password reset
class PasswordResetView(View):
	def get(self, request, confirmation_code, username):
		try:
			user = User.objects.get(username = username)
			profile = user.userprofile

			# Check if the confirmation code is correct
			# Check if the user is active for reset
			if profile.is_password_reset and profile.confirmation_code == confirmation_code:
				password_reset_form = PasswordResetForm()

				return render(request, 'accounts/passreset.html', {'page_title' : 'Sunami - Password Reset', 'reset' : True, 
					'pass_form' : password_reset_form})

		except:
			pass

		return render(request, 'accounts/passreset.html', {'page_title' : 'Sunami - Password Reset'})

	def post(self, request, confirmation_code, username):
		changed = False

		try:
			user = User.objects.get(username = username)
			profile = user.userprofile

			# Just in case someone tries to break in
			if not profile.is_password_reset or profile.confirmation_code != confirmation_code:
				return render(request, 'accounts/passreset.html', {'page_title' : 'Sunami - Password Reset'})

			password_reset_form = PasswordResetForm(data = request.POST)

			if password_reset_form.is_valid():
				profile.is_password_reset = False
				profile.save()

				user.set_password(password_reset_form.cleaned_data.get('password'))
				user.save()

				changed = True

			return render(request, 'accounts/passreset.html', {'page_title' : 'Sunami - Password Reset', 
				'pass_form' : password_reset_form, 'changed' : changed, 'reset' : True})

		except:
			pass

		return render(request, 'accounts/passreset.html', {'page_title' : 'Sunami - Password Reset'})

# Class to register a new user profile
# Write tests for this
class SignUpView(View):
	def get(self, request):
		user_form = UserForm()

		return render(request, 'accounts/signup.html', {'user_form' : user_form, 'registered' : False, 
			'page_title' : 'Sunami - Sign Up'})

	def post(self, request):
		registered = False

		user_form = UserForm(data = request.POST)

		if user_form.is_valid():
			user = user_form.save()

			# Required to hash the password
			user.set_password(user.password)
			user.is_active = False
			user.save()

			# DateTimes automatically added in models.py
			profile = UserProfile(user = user, confirmation_code = generate_code())
			profile.save()

			send_registration_confirmation(user)

			registered = True

		else:
			print(user_form.errors)

		return render(request, 'accounts/signup.html', {'user_form' : user_form, 'registered' : registered, 
			'page_title' : 'Sunami - Sign Up'})