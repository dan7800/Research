FILE_NAME := AndroidData_IEEE

UNAME := $(shell uname)

ifeq ($(UNAME),Linux)
PDF_CMD := acroread
else ifeq ($(UNAME),Darwin)
PDF_CMD := open
else
PDF_CMD := cygstart
endif

TEMP_FILES := \
	$(FILE_NAME).aux \
	$(FILE_NAME).bbl \
	$(FILE_NAME).log \
	$(FILE_NAME).blg \
	$(FILE_NAME).brf \
	$(FILE_NAME).out

DEBUG=no
ifneq '$(DEBUG)' 'yes'
LATEX_FLAGS="-interaction=batchmode"
endif

all: build_paper clean open_pdf

build_paper:
	pdflatex $(LATEX_FLAGS) $(FILE_NAME)
	bibtex $(FILE_NAME)
	pdflatex $(LATEX_FLAGS) $(FILE_NAME)
	pdflatex $(LATEX_FLAGS) $(FILE_NAME)

clean:
	rm -f $(TEMP_FILES)

open_pdf:
	$(PDF_CMD) $(FILE_NAME).pdf
