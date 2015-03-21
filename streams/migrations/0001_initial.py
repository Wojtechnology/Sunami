# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime
from django.utils.timezone import utc


class Migration(migrations.Migration):

    dependencies = [
        ('accounts', '0003_auto_20150321_0200'),
    ]

    operations = [
        migrations.CreateModel(
            name='CommentStream',
            fields=[
                ('id', models.AutoField(verbose_name='ID', primary_key=True, auto_created=True, serialize=False)),
                ('pub_date', models.DateTimeField(default=datetime.datetime(2015, 3, 21, 2, 0, 27, 745692, tzinfo=utc), verbose_name='Publish Date')),
                ('text', models.CharField(max_length=500, verbose_name='Comment Text')),
                ('likes', models.IntegerField(default=0, verbose_name='Comment Likes')),
                ('owner', models.ForeignKey(to='accounts.UserProfile')),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='SoundCloudSong',
            fields=[
                ('id', models.AutoField(verbose_name='ID', primary_key=True, auto_created=True, serialize=False)),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Stream',
            fields=[
                ('id', models.AutoField(verbose_name='ID', primary_key=True, auto_created=True, serialize=False)),
                ('is_active', models.BooleanField(default=False, verbose_name='Stream is Active')),
                ('stream_title', models.CharField(max_length=200, verbose_name='Title of Stream')),
                ('genre', models.CharField(max_length=50, verbose_name='Genre of Stream')),
                ('num_songs', models.IntegerField(default=0, verbose_name='Number of Songs')),
                ('cur_num_listeners', models.IntegerField(default=0, verbose_name='Current Number of Listeners')),
                ('max_num_listeners', models.IntegerField(default=0, verbose_name='Maximum Number of Listeners')),
                ('favourites', models.IntegerField(default=0, verbose_name='Number of Likes')),
                ('comments', models.IntegerField(default=0, verbose_name='Number of Comments')),
                ('start_date_time', models.DateTimeField(default=datetime.datetime(2015, 3, 21, 2, 0, 27, 745158, tzinfo=utc), verbose_name='Stream Start Time')),
                ('end_date_time', models.DateTimeField(default=datetime.datetime(2015, 3, 21, 2, 0, 27, 745179, tzinfo=utc), verbose_name='Stream End Time')),
                ('owner', models.ForeignKey(to='accounts.UserProfile')),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.AddField(
            model_name='soundcloudsong',
            name='stream',
            field=models.ForeignKey(to='streams.Stream'),
            preserve_default=True,
        ),
        migrations.AddField(
            model_name='commentstream',
            name='stream',
            field=models.ForeignKey(to='streams.Stream'),
            preserve_default=True,
        ),
    ]
