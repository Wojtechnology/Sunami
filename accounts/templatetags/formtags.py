from django import template

# Class to add attributes to forms within HTML
register = template.Library()

@register.filter(name = 'add_attributes')
def add_attributes(field, attributes):
	attrs = {}
	definition = attributes.split(',')

	for d in definition:
		if '=' not in d:
			attrs[d] = ''
		else:
			t, v = d.split('=')
			attrs[t] = v

	return field.as_widget(attrs = attrs)