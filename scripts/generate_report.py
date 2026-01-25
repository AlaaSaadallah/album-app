import os
import requests
from datetime import date
from reportlab.platypus import SimpleDocTemplate, Paragraph
from reportlab.lib.styles import getSampleStyleSheet

REPO = os.environ["GITHUB_REPOSITORY"]
TOKEN = os.environ["GITHUB_TOKEN"]
TODAY = date.today().isoformat()

headers = {
    "Authorization": f"token {TOKEN}",
    "Accept": "application/vnd.github+json"
}

url = f"https://api.github.com/repos/{REPO}/issues"
params = {
    "state": "all",
    "since": f"{TODAY}T00:00:00Z"
}

response = requests.get(url, headers=headers, params=params)
issues = response.json()

opened = [i for i in issues if i["state"] == "open"]
closed = [i for i in issues if i["state"] == "closed"]

doc = SimpleDocTemplate("daily_issues_report.pdf")
styles = getSampleStyleSheet()
story = []

story.append(Paragraph(f"<b>Daily Issues Report – {TODAY}</b>", styles["Title"]))
story.append(Paragraph(f"Opened Issues Today: {len(opened)}", styles["Normal"]))
story.append(Paragraph(f"Closed Issues Today: {len(closed)}", styles["Normal"]))
story.append(Paragraph("<br/>", styles["Normal"]))

for issue in issues:
    story.append(
        Paragraph(
            f"- #{issue['number']} {issue['title']} "
            f"({issue['state']}) - {issue.get('assignee', {}).get('login', 'Unassigned')}",
            styles["Normal"]
        )
    )

doc.build(story)
