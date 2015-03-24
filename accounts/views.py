from django.shortcuts import render, redirect
from django.contrib.auth import authenticate, login, logout
from django.views.generic import View
from django.utils import timezone
from django.contrib.auth.models import User
from django.contrib.auth.decorators import login_required
from accounts.models import UserProfile
from accounts.forms import UserForm

# Login required wrapper for as_view method for class views
class LoginRequiredMixin(object):
	@classmethod
	def as_view(cls, **initkwargs):
		view = super(LoginRequiredMixin, cls).as_view(**initkwargs)
		return login_required(view)

# Class to login to user profile
# Write tests for this
class LoginView(View):
	def get(self, request):
		# If user already logged in, redirect to home page
		if request.user.is_authenticated():
			return redirect('index')

		return render(request, 'accounts/login.html')

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

		return render(request, 'accounts/login.html', {'errors' : errors[0:1]})


# Class to logout and redirect 
class LogoutView(LoginRequiredMixin, View):
	def get(self, request):
		logout(request)
		return redirect('index')

# Generate confirmation code
def generate_code():
	confirmation_code = ''.join(random.SystemRandom().choice(string.ascii_uppercase + string.digits) for i in range(33))
	return confirmation_code

# Send Registration Email
def send_registration_confirmation(user):
	p = user.userprofile
	title = 'Crowdle account confirmation'
	content = 'http://127.0.0.1:8000/profile/confirm/' + p.authentication_code + '/' + user.username
	send_mail(title, content, 'sunamisound@gmail.com', [user.email])

# Class to authenticate user

# Class to register a new user profile
# Write tests for this
class SignUpView(View):

	def get(self, request):
		user_form = UserForm()

		return render(request, 'accounts/signup.html', {'user_form' : user_form, 'registered' : False})

	def post(self, request):
		registered = False

		user_form = UserForm(data = request.POST)

		if user_form.is_valid():
			user = user_form.save()

			# Required to hash the password
			user.set_password(user.password)
			user.save()

			# DateTimes automatically added in models.py
			profile = UserProfile(user = user)
			profile.save()

			registered = True

		else:
			print(user_form.errors)

		return render(request, 'accounts/signup.html', {'user_form' : user_form, 'registered' : registered})