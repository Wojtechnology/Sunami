from django.conf.urls import patterns, url
from accounts.views import SignUpView, LoginView, LogoutView, ConfirmView

urlpatterns = patterns('',

    url(r'^login/$', LoginView.as_view(), name = 'login'),
    url(r'^signup/$', SignUpView.as_view(), name = 'signup'),
    url(r'^logout/$', LogoutView.as_view(), name = 'logout'),
    url(r'^confirm/(?P<confirmation_code>[a-zA-Z0-9]+)/(?P<username>[a-zA-Z0-9]+)/$', ConfirmView.as_view(), name = 'confirm'),
	
	)