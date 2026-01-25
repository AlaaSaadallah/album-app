#!/usr/bin/env python3
import os
import sys
import argparse
import requests
from datetime import datetime
from collections import defaultdict
from reportlab.lib.pagesizes import LETTER
from reportlab.lib import colors
from reportlab.lib.units import inch
from reportlab.pdfgen import canvas
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, Image
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.graphics.shapes import Drawing
from reportlab.graphics.charts.piecharts import Pie
from reportlab.graphics.charts.barcharts import VerticalBarChart

# -----------------------
# Parse optional arguments
# -----------------------
parser = argparse.ArgumentParser(description="Generate Issues Report PDF with Date Range")
parser.add_argument(
    "--from-date",
    type=str,
    help="Start date in YYYY-MM-DD format (default: yesterday)",
)
parser.add_argument(
    "--to-date",
    type=str,
    help="End date in YYYY-MM-DD format (default: today)",
)
parser.add_argument(
    "--owner",
    type=str,
    default="your-org-or-username",
    help="GitHub repository owner",
)
parser.add_argument(
    "--repo",
    type=str,
    default="your-repo-name",
    help="GitHub repository name",
)
args = parser.parse_args()

from datetime import timedelta

# Set date range
if args.from_date:
    from_date = datetime.strptime(args.from_date, "%Y-%m-%d")
else:
    from_date = datetime.today() - timedelta(days=1)

if args.to_date:
    to_date = datetime.strptime(args.to_date, "%Y-%m-%d")
else:
    to_date = datetime.today()

from_date_str = from_date.strftime("%Y-%m-%d")
to_date_str = to_date.strftime("%Y-%m-%d")

OWNER = args.owner
REPO = args.repo

# -----------------------
# GitHub API setup
# -----------------------
GITHUB_TOKEN = os.getenv("GITHUB_TOKEN")
if not GITHUB_TOKEN:
    print("Error: GITHUB_TOKEN environment variable is not set")
    sys.exit(1)

headers = {"Authorization": f"token {GITHUB_TOKEN}"}

# Fetch issues created or updated within the date range
# Using 'since' parameter to get issues updated since from_date
since_param = from_date.strftime("%Y-%m-%dT00:00:00Z")
issues_url = f"https://api.github.com/repos/{OWNER}/{REPO}/issues?state=all&since={since_param}&per_page=100"

# -----------------------
# Fetch issues
# -----------------------
try:
    response = requests.get(issues_url, headers=headers)
    response.raise_for_status()
    issues = response.json()
except Exception as e:
    print(f"Error fetching issues from GitHub: {e}")
    sys.exit(1)

# -----------------------
# Process issues data
# -----------------------
total_issues = 0
bug_count = 0
enhancement_count = 0
other_count = 0
open_count = 0
closed_count = 0

# Contributor stats: {username: {'bugs': count, 'open': count, 'closed': count}}
contributor_stats = defaultdict(lambda: {'bugs': 0, 'enhancements': 0, 'other': 0, 'open': 0, 'closed': 0, 'total': 0})

# Filter issues by date range
filtered_issues = []

for issue in issues:
    # Skip pull requests
    if 'pull_request' in issue:
        continue
    
    # Parse issue dates
    created_at = datetime.strptime(issue['created_at'], "%Y-%m-%dT%H:%M:%SZ")
    updated_at = datetime.strptime(issue['updated_at'], "%Y-%m-%dT%H:%M:%SZ")
    
    # Check if issue was created or updated within the date range
    if (from_date <= created_at <= to_date + timedelta(days=1)) or \
       (from_date <= updated_at <= to_date + timedelta(days=1)):
        filtered_issues.append(issue)

# Now process the filtered issues
for issue in filtered_issues:
    total_issues += 1
    # Skip pull requests
    if 'pull_request' in issue:
        continue
    
    # Count by state
    if issue['state'] == 'open':
        open_count += 1
    else:
        closed_count += 1
    
    # Count by label (bug detection)
    labels = [label['name'].lower() for label in issue.get('labels', [])]
    is_bug = any('bug' in label for label in labels)
    is_enhancement = any('enhancement' in label or 'feature' in label for label in labels)
    
    if is_bug:
        bug_count += 1
    elif is_enhancement:
        enhancement_count += 1
    else:
        other_count += 1
    
    # Get assignee (handle None case)
    assignee = issue.get('assignee')
    assignee_login = assignee['login'] if assignee else 'Unassigned'
    
    # Update contributor stats
    contributor_stats[assignee_login]['total'] += 1
    if issue['state'] == 'open':
        contributor_stats[assignee_login]['open'] += 1
    else:
        contributor_stats[assignee_login]['closed'] += 1
    
    if is_bug:
        contributor_stats[assignee_login]['bugs'] += 1
    elif is_enhancement:
        contributor_stats[assignee_login]['enhancements'] += 1
    else:
        contributor_stats[assignee_login]['other'] += 1

# -----------------------
# Create graphs
# -----------------------

def create_pie_chart(data, labels, title, filename):
    """Create a pie chart and save as image"""
    drawing = Drawing(400, 200)
    pie = Pie()
    pie.x = 150
    pie.y = 50
    pie.width = 100
    pie.height = 100
    pie.data = data
    pie.labels = labels
    pie.slices.strokeWidth = 0.5
    
    # Color scheme
    colors_list = [colors.HexColor('#FF6B6B'), colors.HexColor('#4ECDC4'), 
                   colors.HexColor('#45B7D1'), colors.HexColor('#FFA07A'),
                   colors.HexColor('#98D8C8'), colors.HexColor('#F7DC6F')]
    for i, color in enumerate(colors_list[:len(data)]):
        pie.slices[i].fillColor = color
    
    drawing.add(pie)
    drawing.save(formats=['png'], outDir='.', fnRoot=filename)
    return f"{filename}.png"

def create_bar_chart(data, labels, title, filename, x_label="", y_label=""):
    """Create a bar chart and save as image"""
    drawing = Drawing(400, 300)
    bc = VerticalBarChart()
    bc.x = 50
    bc.y = 50
    bc.height = 200
    bc.width = 300
    bc.data = [data]
    bc.categoryAxis.categoryNames = labels
    bc.valueAxis.valueMin = 0
    bc.valueAxis.valueMax = max(data) * 1.2 if data else 10
    bc.bars[0].fillColor = colors.HexColor('#4ECDC4')
    bc.categoryAxis.labels.angle = 45
    bc.categoryAxis.labels.fontSize = 8
    
    drawing.add(bc)
    drawing.save(formats=['png'], outDir='.', fnRoot=filename)
    return f"{filename}.png"

# Create overall status pie chart
if open_count + closed_count > 0:
    status_chart = create_pie_chart(
        [open_count, closed_count],
        ['Open', 'Closed'],
        'Issue Status',
        'status_chart'
    )
else:
    status_chart = None

# Create issue type pie chart
if bug_count + enhancement_count + other_count > 0:
    type_chart = create_pie_chart(
        [bug_count, enhancement_count, other_count],
        ['Bugs', 'Enhancements', 'Other'],
        'Issue Types',
        'type_chart'
    )
else:
    type_chart = None

# -----------------------
# Generate PDF
# -----------------------
pdf_file = f"issues_report_{from_date_str}_to_{to_date_str}.pdf"
doc = SimpleDocTemplate(pdf_file, pagesize=LETTER)
styles = getSampleStyleSheet()
story = []

# Custom styles
title_style = ParagraphStyle(
    'CustomTitle',
    parent=styles['Heading1'],
    fontSize=20,
    textColor=colors.HexColor('#2C3E50'),
    spaceAfter=30,
    alignment=TA_CENTER
)

heading_style = ParagraphStyle(
    'CustomHeading',
    parent=styles['Heading2'],
    fontSize=14,
    textColor=colors.HexColor('#34495E'),
    spaceAfter=12,
    spaceBefore=12
)

# Title
title = Paragraph(f"Issues Report - {from_date_str} to {to_date_str}", title_style)
story.append(title)
story.append(Spacer(1, 0.2 * inch))

# Repository info
repo_info = Paragraph(f"<b>Repository:</b> {OWNER}/{REPO}", styles['Normal'])
story.append(repo_info)
date_range_info = Paragraph(f"<b>Date Range:</b> {from_date_str} to {to_date_str}", styles['Normal'])
story.append(date_range_info)
story.append(Spacer(1, 0.3 * inch))

# Summary Section
summary_heading = Paragraph("Summary", heading_style)
story.append(summary_heading)

summary_data = [
    ['Metric', 'Count'],
    ['Total Issues', str(total_issues)],
    ['Open Issues', str(open_count)],
    ['Closed Issues', str(closed_count)],
    ['Bugs', str(bug_count)],
    ['Enhancements', str(enhancement_count)],
    ['Other', str(other_count)],
]

summary_table = Table(summary_data, colWidths=[3 * inch, 2 * inch])
summary_table.setStyle(TableStyle([
    ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#34495E')),
    ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
    ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
    ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
    ('FONTSIZE', (0, 0), (-1, 0), 12),
    ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
    ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
    ('GRID', (0, 0), (-1, -1), 1, colors.black),
]))
story.append(summary_table)
story.append(Spacer(1, 0.3 * inch))

# Charts Section
if status_chart or type_chart:
    charts_heading = Paragraph("Visual Overview", heading_style)
    story.append(charts_heading)
    
    if status_chart:
        story.append(Paragraph("<b>Issue Status Distribution</b>", styles['Normal']))
        story.append(Spacer(1, 0.1 * inch))
        status_img = Image(status_chart, width=4*inch, height=2*inch)
        story.append(status_img)
        story.append(Spacer(1, 0.2 * inch))
    
    if type_chart:
        story.append(Paragraph("<b>Issue Type Distribution</b>", styles['Normal']))
        story.append(Spacer(1, 0.1 * inch))
        type_img = Image(type_chart, width=4*inch, height=2*inch)
        story.append(type_img)
        story.append(Spacer(1, 0.3 * inch))

# Contributor Statistics Section
story.append(PageBreak())
contrib_heading = Paragraph("Contributor Statistics", heading_style)
story.append(contrib_heading)

# Sort contributors by total issues (descending)
sorted_contributors = sorted(
    contributor_stats.items(),
    key=lambda x: x[1]['total'],
    reverse=True
)

for contributor, stats in sorted_contributors:
    story.append(Spacer(1, 0.2 * inch))
    
    # Contributor name
    contrib_name = Paragraph(f"<b>{contributor}</b>", styles['Heading3'])
    story.append(contrib_name)
    story.append(Spacer(1, 0.1 * inch))
    
    # Stats table
    contrib_data = [
        ['Metric', 'Count'],
        ['Total Issues', str(stats['total'])],
        ['Open', str(stats['open'])],
        ['Closed', str(stats['closed'])],
        ['Bugs', str(stats['bugs'])],
        ['Enhancements', str(stats['enhancements'])],
        ['Other', str(stats['other'])],
    ]
    
    contrib_table = Table(contrib_data, colWidths=[2.5 * inch, 1.5 * inch])
    contrib_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#4ECDC4')),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('FONTSIZE', (0, 0), (-1, 0), 10),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 8),
        ('BACKGROUND', (0, 1), (-1, -1), colors.lightgrey),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.black),
    ]))
    story.append(contrib_table)

# Issues List Section
story.append(PageBreak())
issues_heading = Paragraph("All Issues", heading_style)
story.append(issues_heading)
story.append(Spacer(1, 0.2 * inch))

if not filtered_issues:
    story.append(Paragraph("No issues found in the specified date range.", styles['Normal']))
else:
    # Create issues table
    issues_data = [['#', 'Title', 'State', 'Assignee', 'Labels']]
    
    for issue in filtered_issues:
        assignee = issue.get('assignee')
        assignee_login = assignee['login'] if assignee else 'Unassigned'
        
        labels_text = ', '.join([label['name'] for label in issue.get('labels', [])[:3]])
        if len(issue.get('labels', [])) > 3:
            labels_text += '...'
        
        # Truncate title if too long
        title = issue['title']
        if len(title) > 50:
            title = title[:47] + '...'
        
        issues_data.append([
            str(issue['number']),
            title,
            issue['state'].capitalize(),
            assignee_login,
            labels_text or '-'
        ])
    
    issues_table = Table(issues_data, colWidths=[0.5*inch, 2.5*inch, 0.8*inch, 1.2*inch, 1.5*inch])
    issues_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#2C3E50')),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('FONTSIZE', (0, 0), (-1, -1), 8),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 8),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.lightgrey]),
    ]))
    story.append(issues_table)

# Build PDF
doc.build(story)

# Clean up chart images
if status_chart and os.path.exists(status_chart):
    os.remove(status_chart)
if type_chart and os.path.exists(type_chart):
    os.remove(type_chart)

print(f"✓ PDF report generated: {pdf_file}")
print(f"✓ Total issues: {total_issues}")
print(f"✓ Contributors analyzed: {len(contributor_stats)}")