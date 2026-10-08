from pathlib import Path
import json
import re

import pypdfium2 as pdfium
from PIL import Image, ImageDraw
from pypdf import PdfReader

directory = Path(__file__).resolve().parent
qa_directory = directory / "verificacion"
qa_directory.mkdir(exist_ok=True)
summaries = []
for stem in ("INFORME_AIKUKISNA", "DICCIONARIO_DATOS"):
    reader = PdfReader(directory / f"{stem}.pdf")
    page_texts = [page.extract_text() for page in reader.pages]
    document = pdfium.PdfDocument(directory / f"{stem}.pdf")
    thumbnails = []
    for number in range(len(document)):
        page = document[number]
        bitmap = page.render(scale=0.6)
        picture = bitmap.to_pil().convert("RGB")
        picture.thumbnail((298, 421))
        thumbnail = Image.new("RGB", (320, 450), "#dde3e9")
        thumbnail.paste(picture, ((320 - picture.width) // 2, 20))
        ImageDraw.Draw(thumbnail).text((12, 3), f"Pagina {number + 1}", fill="black")
        thumbnails.append(thumbnail)
        if number in (0, len(document) // 2, len(document) - 1):
            page.render(scale=1.5).to_pil().save(qa_directory / f"{stem}_{number + 1}.png")
    for offset in range(0, len(thumbnails), 12):
        group = thumbnails[offset:offset + 12]
        sheet = Image.new("RGB", (1280, ((len(group) + 3) // 4) * 450), "white")
        for index, thumbnail in enumerate(group):
            sheet.paste(thumbnail, ((index % 4) * 320, (index // 4) * 450))
        sheet.save(qa_directory / f"{stem}_contacto_{offset // 12 + 1}.png")
    summaries.append({
        "documento": stem,
        "paginas": len(reader.pages),
        "palabras_aproximadas": sum(len(re.findall(r"\S+", text)) for text in page_texts),
        "paginas_casi_vacias": [index + 1 for index, text in enumerate(page_texts) if len(text.strip()) < 100],
    })
print(json.dumps(summaries, ensure_ascii=True, indent=2))
