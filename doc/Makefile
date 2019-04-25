PDFLATEX = pdflatex

doc : BouMaton.pdf BouMaton_fr.pdf

%.pdf : %.tex
	$(PDFLATEX) $<
	$(PDFLATEX) $<
	$(PDFLATEX) $<

clean :
	rm -f *.aux *.log *.out *.synctex.gz *.fdb_latexmk

clobber : clean
	rm -r *.pdf
