git tag $(date +%Y%m%d_%s)_$(git log -n 1 --pretty=format:"%h")
