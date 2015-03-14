from django.conf.urls import patterns, include, url
from django.contrib import admin

from sunami.views import IndexView, LoginView

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'sunami.views.home', name='home'),
    # url(r'^blog/', include('blog.urls')),

    url(r'^admin/', include(admin.site.urls)),
    url(r'^$', IndexView.as_view(), name = "index"),
    url(r'^login/', LoginView.as_view(), name = "login"),
 
)