find . -type f -not \( -path "*target*" -or -path "*.git*" -or -path "*-gen*" -or -path "*.classpath*" -or -path "*.project*" -prune \) -exec grep -Iq . {} \; -print0 | xargs -0 sed -i "s/\t/    /g"
