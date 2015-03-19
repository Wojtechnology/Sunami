from django.conf.urls import patterns, url
from accounts.views import SignUpView, LoginView, LogoutView

urlpatterns = patterns('',

    url(r'^login/$', LoginView.as_view(), name = 'login'),
    url(r'^signup/$', SignUpView.as_view(), name = 'signup'),
    url(r'^logout/$', LogoutView.as_view(), name = 'logout'),
	
	)