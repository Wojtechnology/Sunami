# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('accounts', '0003_auto_20150321_0200'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='userprofile',
            name='create_date',
        ),
        migrations.RemoveField(
            model_name='userprofile',
            name='update_date',
        ),
        migrations.AddField(
            model_name='userprofile',
            name='confirmation_code',
            field=models.CharField(default='', max_length=50, verbose_name='Confirmation Code'),
            preserve_default=True,
        ),
    ]
